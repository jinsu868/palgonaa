<p align="center">
   <img width="300" alt="2025-03-08_20-37-02" src="https://github.com/user-attachments/assets/bc22d291-ec07-44cd-8328-91818870bf06" />
</p>


<p align="center">
   중고 물품 경매 서비스, Palgona
</p>

# 프로젝트 소개

중고로 물품을 판매할 때 가격 측정에 어려움도 있고 한 사람이 여러 사람에게 동시에 구매 약속을 잡는 문제가 있었습니다.
저희 Palgona 서비스를 사용하면 중고 물품을 적정한 가격에 쉽게 경매를 통해 판매가 가능합니다.

# 기여한 부분

## 입찰 시스템 구축

### V1 입찰 플로우 (개선 전)

* 동시에 입찰을 진행할 때 발생하는 Lost Update를 막기 위해 처음에는 PorductId에 Lock을 잡음으로써 Serializable 하게 입찰을 진행했습니다. (V1) 

<img width="1153" alt="2025-03-08_21-31-53" src="https://github.com/user-attachments/assets/50b88662-2edf-40cb-bba6-dcdb621bd614" /></br>

이 때 3가지 옵션정도 고려했습니다.

1. DB - SELECT FOR UPDATE (x)
2. Versioning (x)
3. Redis Distributed Lock (v1) dev-v1

1, 2 번에 비해 3번이 유리하다고 판단했고 Redis Distributed Lock을 채택했습니다. (자세한 내용은 개인 노션에 정리)

* V1 방식의 경우 Redis 에 장애가 발생한 경우 입찰을 진행할 수 없는 단점이 있습니다.
* 물론 Clustering을 하게 되면 물리적으로 동시에 모든 Node가 내려가진 않는 이상 문제가 발생하지 않습니다.
* 하지만 이런 경우가 진짜 드물겠지만 발생할 수도 있고 온라인 경매 입찰의 서비스의 성격을 고려헀을 때 꼭 즉각적으로 락을 잡고 갱신을 할 필요가 있는지 고민해볼 필요가 있습니다.
  * "입찰 로그를 쌓아두고 짧은 주기로 현재 입찰가를 갱신해주고 화면에 반영해도 충분하지 않은가?" 라는 생각을 해볼 수 있습니다.

</br>

### V2 입찰 플로우 (개선 후)
현재 시점에 유효한 입찰 시도 이벤트(bid_event)를 DB에 append only로 쌓고 짧은 주기로 스케줄링을 돌려서 현재 입찰에 대한 정보(bid)를 갱신합니다. (V2)
<img width="895" alt="2025-05-23_19-38-46" src="https://github.com/user-attachments/assets/9d7c275b-63cb-4b4d-9ce6-79b573633832" />

* bid 테이블에서 현재 입찰가를 보고 지금보다 더 높은 가격으로 들어오는 bid_event를 모두 append only로 쌓는다.
* 주기적으로 Batch job을 통해 bid_event -> bid 테이블로 현재 입찰가를 갱신한다. (snapshot 생성)
  * bid_event만 가지고도 현재 입찰가와 입찰자를 알아낼 수 있지만 매번 Read를 할 때마다 모든 Record를 읽으면 성능이 나오지 않습니다.
  * CQRS 패턴을 도입하여 주기적으로 Batch Job을 통해 Bid(현재 입찰 정보 snapshot)을 UPDATE 치고 조회는 Bid에서 하면 성능이 좋습니다.

</br>

#### bid_event 테이블 (JPA 선택 X 이유)
<img width="872" alt="2025-05-23_19-00-29" src="https://github.com/user-attachments/assets/39d5c047-bce0-45d0-82cc-529cfa6935a7" />

* bid_event의 경우 append only로 테이블에 INSERT만 발생하는 테이블입니다.
* 때문에 굳이 ORM(JPA)를 사용하여 Layer를 하나 더 둠으로써 성능 손해를 볼 필요가 없습니다. (JDBC 작성해서 Insert 처리)

</br>

## 상품 조회 성능 개선

### ProductMeta Table 도출
<img width="896" alt="2025-05-23_19-10-50" src="https://github.com/user-attachments/assets/d907b4e7-9d1f-4c83-89ab-5574c73f8a1f" />

* 저는 위와 같이 상품에 대한 통계 테이블을 만들었습니다. bookmarkCount가 높을 수록, 그리고 Product 등록이 얼마 안지났을수록 먼저 조회되도록 score를 세팅했습니다.
* 상품에 대한 가중치는 Strong Consistency가 굳이 필요없습니다.
* 일시적으로 오차가 발생해도 크게 문제될 것이 없기에 eventually consistency만 맞춰줬습니다.

</br>

### Product List 조회 방식
<img width="936" alt="2025-05-23_19-22-31" src="https://github.com/user-attachments/assets/dc81069d-0a55-408e-921b-75797c16ef45" />

* ProductMeta 테이블을 활용하여 가져올 상품의 Id를 가져옵니다. (DEFAULT_PAGE_SIZE = 10)
* 그리고 실제 Product 테이블에서 In Query를 통해 필요한 데이터를 가져옵니다.
* 가져온 데이터를 정렬 기준에 따라 정렬하고 리턴합니다. (메모리 정렬이 가능한 이유는 ProductMeta 테이블에서 조회한 Product Id로 가져온 것이기 때문에 조건에 맞는 데이터만 가져온 것.)


## 시스템 아키텍처

<img width="1023" alt="2025-03-08_21-00-32" src="https://github.com/user-attachments/assets/2edf7c51-5a74-4d45-8f7e-d407d5e6e77c" />

## 
