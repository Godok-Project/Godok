<p align = "center">
<img width="85%" height = "550" alt="Godok_concept_1" src="https://user-images.githubusercontent.com/119986573/232371260-1fec27ee-e74d-4be8-940a-03e3f60f0f44.png" >
</p>
  
# Godok 📚
### Godok은 1000만권 이상의 대용량 책 데이터를 활용해 다양한 필터를 적용하여 빠른 검색 결과와 구매를 할 수 있는 서비스 입니다.


# 프로젝트 설명
- 프로젝트 이름 : Godok(고독, Go讀)
- 프로젝트 목표
    - 검색 최적화 : 10,000,000 건 이상의 책 정보를 다양한 검색 조건에 따라 고객에게 제공
    - 동시성 제어 : 동일한 책에 대해 동시에 여러 개의 주문 발생 시 누락 없는 주문 처리
    - 검색 자동 완성
    - 기간별 랭킹 서비스 : 일정 기간 동안 판매된 책 정보를 고객에게 제공하여 책 추천
- 프로젝트 둘러보기
    - [Godok 둘러보기](http://15.165.243.65:8080/)
    - [Godok Notion](https://great-waltz-aa7.notion.site/go-f7836e2681ca449598aeeac7401b890a)

# Crew
|-|이름|주특기|<img src="https://img.shields.io/badge/Github-181717?style=flat&logo=github&logoColor=white"/>|
|----|----|-----|------------|
|👩🏻‍💻|양윤선|<img src="https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white"/>|https://github.com/hobambi|
|👩🏻‍💻|이현빈|<img src="https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white"/>|https://github.com/HBLEEEEE|
|👩🏻‍💻|조성재|<img src="https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white"/>|https://github.com/ssungcohol|
|👩🏻‍💻|한승현|<img src="https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white"/>|https://github.com/gkstmdgus|

# 기술스택 💻
### Application
<img src="https://img.shields.io/badge/HTML-E34F26?style=flat&logo=HTML5&logoColor=white"/>  <img src="https://img.shields.io/badge/CSS-1572B6?style=flat&logo=CSS3&logoColor=white"/>  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat&logo=JavaScript&logoColor=white"/>  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=thymeleaf&logoColor=white"/>

<img src="https://img.shields.io/badge/Java-007396?style=flat&logo=Java&logoColor=white"/> <img src="https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=Spring&logoColor=white"/> <img src="https://img.shields.io/badge/Spring Boot-6DB33f?style=flat&logo=Spring Boot&logoColor=white"/> 

### Database
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=MySQL&logoColor=white"/> <img src="https://img.shields.io/badge/Amazon RDS-527FFF?style=flat&logo=amazonrds&logoColor=white"/> <img src="https://img.shields.io/badge/Redis-DC382D?style=flat&logo=Redis&logoColor=white"/>

### Infra
<img src="https://img.shields.io/badge/Amazon AWS-232F3E?style=flat&logo=Amazon AWS&logoColor=white"/> <img src="https://img.shields.io/badge/Amazon EC2-FF9900?style=flat&logo=Amazon EC2&logoColor=white"/> <img src="https://img.shields.io/badge/Amazon S3-569A31?style=flat&logo=Amazon S3&logoColor=white"/> 

<img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=flat&logo=GitHub Actions&logoColor=white"/> 

### Test
<img src="https://img.shields.io/badge/Postman-FF6C37?style=flat&logo=Postman&logoColor=white"/> <img src="https://img.shields.io/badge/JMeter-D22128?style=flat&logo=apachejmeter&logoColor=white"/>

### Search
<img src="https://img.shields.io/badge/ElasticSearch-005571?style=flat&logo=Elasticsearch&logoColor=white"/> <img src="https://img.shields.io/badge/Logstash-005571?style=flat&logo=Logstash&logoColor=white"/> <img src="https://img.shields.io/badge/Kibana-005571?style=flat&logo=kibana&logoColor=white"/> 

### Communication
<img src="https://img.shields.io/badge/Slack-4A154B?style=flat&logo=Slack&logoColor=white"/> <img src="https://img.shields.io/badge/KaKaoTalk-FFCD00?style=flat&logo=KaKaoTalk&logoColor=white"/> <img src="https://img.shields.io/badge/Notion-000000?style=flat&logo=Notion&logoColor=white"/> 

# 아키텍처 📐
![아키텍처_실전_v3](https://user-images.githubusercontent.com/119986573/232654837-fb21873d-a2de-427d-b561-49330f95c782.png)

# ERD 📂
<details>
<summary>펼쳐보기</summary>
<div>

<img width="85%" height = "550" alt="ERD Final" src="https://user-images.githubusercontent.com/119986573/232374057-c013bf41-a298-4581-8c42-80d44f13986d.PNG">

</div>
</details>

# 트러블 슈팅 🛠️

<details>
<summary>검색 성능 개선</summary>
  
<div>
  
  1. 일반인덱스 - 필터에 index 적용 후 키워드는 like를 이용한 검색
  
   - 인덱스를 걸지 않으면 필터 적용 시 서버로부터 응답을 받지 못했다. 필터에 인덱스를 적용하여 검색 속도가 향상되기는 했지만 모든 필터를 적용하기에는 무리가 있었다.
  
    - 하나의 쿼리에는 하나의 인덱스만 적용이 가능
  
    - 모든 필터를 복합 인덱스로 묶으면 기능이 한정
  
    - like로 모든 문서를 탐색하는 것은 비효율적
  
  2. Full text 인덱스 - title에 Fulltext 인덱스를 적용한 검색
  
   - Mysql의 Fulltext index를 적용해 검색 속도를 향상시키려고 시도. Fulltext index는 역색인 방식으로 색이하므로 키워드 검색 시 빠른 속도를 보여주지만, 한계점도 명확.
  
    - 기본 parser의 한계
  
    - 필터를 여러개 적용 시 느려지는 현상 발생
  
    - 정확도 문제 발생
  
  3. Elastic Search - Mysql 데이터를 동기화하여 검색 엔진으로 사용
  
    - 한글 형태소 분석기 (Nori Tokenizer)를 사용하여 title, author를 역색인
  
      - Ngram, Stop-word보다 세밀한 한글 분석이 가능해졌고 검색 결과의 정확도가 올라갔다.
  
    - 하나의 쿼리에 여러 인덱스 사용 가능
  
      - 검색 속도가 향상  
    
</div>
</details>

<details>
<summary>동시성 제어</summary>
  
<div>
  
  1. Optimistic vs Pessimistic
  
   - Optimistic
  
    - NONE : 별도의 옵션을 사용하지 않아도 Entity에 @Version이 적용된 필드만 있으면 낙관적 잠금이 적용
  
    - OPTIMISTIC(read) : Entity 수정 시에만 발생하는 낙관적 잠금이 읽기 시에도 발생하도록 설정합니다.읽기시에도 버전을 체크하고 트랜잭션이 종료될 때까지 다른 트랜잭션에서 변경하지 않음을 보장
  
    - OPTIMISTIC_FORCE_INCREMENT(write) : 낙관적 잠금을 사용하면서 버전 정보를 강제로 증가 시키는 옵션
  
   - Pessimistic
  
    - PESSIMISTIC_READ : dirty read가 발생하지 않을 때마다 공유 잠금(Shared Lock)을 획득하고 데이터가 UPDATE, DELETE 되는 것을 방지 할 수 있습니다.
  
    - PESSIMISTIC_WRITE : 배타적 잠금(Exclusive Lock)을 획득하고 데이터를 다른 트랜잭션에서 READ, UPDATE, DELETE 하는 것을 방지 할 수 있습니다.
  
    - ESSIMISTIC_FORCE_INCREMENT : 이 잠금은 PESSIMISTIC_WRITE와 유사하게 작동 하지만 @Version이 지정된 Entity와 협력하기 위해 도입되어 PESSIMISTIC_FORCE_INCREMENT 잠금을 획득할 시 버전이 업데이트 됩니다.
  
   - Optimistic과 Pessimistic 실험 결과
  
    - 현재 서비스에서는 PESSIMISTIC_WRITE만 제대로 작동한다.
  
    - Optimistic은 성능은 좋지만 주문이 폭주하는 경우 트랜잭션을 시작할 때 저장한 버전 값과 트랜잭션이 끝나고 난 후 체크한 버전 값이 다른 경우가 많은데 이 때 잘 작동하지 않는다. 따라서 진행하는 서비스에는 적합하지 않다.
  
    - 또한 Force_Increment 옵션의 경우 지원하는 DB의 종류가 정해져 있는데, 현재 사용 중인 mysql에서는 사용이 불가능하다.
  
    - PESSIMISTIC_READ는 어떤 트랜잭션이 수정 중일 때 다른 트랜잭션에서 읽기는 가능하다. 하지만 현재 실험에서는 모든 트랜잭션들이 수정하고자 하기 때문에 적합하지 않다.
  
  2. Redisson Distribution Lock
  
    - Transaction이 Lock을 점거하고 있다는 정보를 redis 서버(캐시 서버)에 올려서, 분산 된 서버에서 하나의 DB를 조회하고 수정할 때 사용하기에 적절한 방법이다.
  
    - 아래 자세히 설명하겠지만, 요청 Thread를 순서대로 처리한다는 보장이 없다는 단점이 있다.
  
    - 서비스 로직만 아래와 같이 바꿔준다.
  
      - redis 서버에 lockname을 key로 가진 데이터가 없으면 만들고 lock을 건다
  
      - 이미 lock이 있다면, 기다리거나 포기한다.
  
      - 트랜잭션을 성공적으로 수행했다면 lock을 해제한다.  

</div>
</details>

<details>
<summary>서킷 브레이커</summary>
  
<div>
  
  1. 현재 서비스에서 엘라스틱 서치가 쓰이는 요청
  
    - 검색 버튼을 눌러서 카테고리 + 키워드를 검색하는 경우
  
    - 검색 페이지에서 필터 적용 버튼을 눌러서 필터 검색을 하는 경우
  
    - 페이지 버튼을 눌러 페이지를 이동하는 경우
  
  2. 페이징 문제 발생
  
    - 현재 페이징 방식 = 커서 페이지네이션
  
    - Mysql과 엘라스틱 서치의 커서 값이 달라 문제 발생 (알고리즘이 다름)
  
    - 커서가 다르기 때문에 페이지 이동시에는 추가적인 작업이 필요
  
  3. 대처 가능한 시나리오 - 2가지
  
    - 어떤 페이지에 있던지 간에 서킷 오픈시 1페이지로 리다이렉트 한다.

    - 요청한 페이지 정보를 가지고 커서 값을 모두 찾아와서 커서 리스트를 교체한다.
  
  4. 선택 방법 : 첫 번째 시나리오 선택과 이유
  
    - 적절한 방법은 두 번째 방법이 적합
  
    - 하지만, 우리 서비스에는 첫번째 방법으로 구현
  
    - 이유 : Mysql의 속도
      - 애초에 엘라스틱 서치로 넘어간 이유가 Mysql의 속도였다. 많은 필터를 적용하면 한 페이지를 불러오는데도 시간이 오래 걸려서 문제였다. 그런데 한 페이지가 아닌 여러 페이지의 커서를 모두 찾아오는 로직을 Mysql이 하면, 결과는 안봐도 타임아웃이다. 그리고 가져온다고 한들 클라이언트는 하염없이 로딩창을 보며 기다려야 한다.

</div>
</details>

<details>
<summary>Redis & Batch</summary>
  
<div>
  1. Batch 설계 및 예외 처리
    
    - 주문 데이터의 중요성과 DB 접근성
  
      - DB에 자주 접근하는 것은 좋지않다. 따라서 오늘 주문 데이터를 Redis에서 저장하고 있다가 02시에 일괄적으로 DB에 저장하는 방법을 구상했다.
  
      - 하지만 주문 데이터는 서비스에서 매우 중요하기 때문에 휘발성이 높은 Redis에 저장해 두는 것은 좋지 않다.
  
      - 따라서 redis에는 가장 많이 찾게 되는 main 페이지에서 어제 팔린 책들에 대한 ranking만 넣어 두는 것으로 설계했다.
  
    - 예외 처리
  
      - 현재는 가짜 주문을 추가하여 어제의 bestseller 1~8등까지 들어가나, 실제 서비스에서는 그럴 수 없다.
  
      - 따라서 어제 팔린 책 종류가 8권 미만인 경우 부족한 책들을 채워 주도록 구상하였다.
  
  2. Batch 적용 문제 및 해결
  
    - 문제
  
      - summonRank()라는 복잡한 메서드가 service에 들어가 있는데, batch 로 빼주고 싶었으나 불가능하였다.
  
    - 원인
  
      - @Transaction 안에 batch를 넣을 수 가 없다. Jobrepository의 기본 설정이 외부 트랜잭션을 허용하지 않기 때문이다.
  
      - batch 내부에서 @Transactional을 붙인 메서드를 실행하기 때문인데 만약 더 큰 Transaction안에 들어가 있다면 내부 transaction이 끝나도 커밋시점이 도래하지 않기 때문이다.
  
    - 해결
  
      - 해결 방법으로는 TransactionTemplate가 새로운 트랜잭션을 생성하도록 강제하여 외부 트랜잭션과 별개로 커밋하게 만들어 데드락 문제를 해결할 수 있다. 하지만 외부 트랜잭션과 내부 트랜잭션의 StepExecution 상태의 일관성이 깨질 수 있게 될 수 도 있다.
  
      - 따라서 Job 실행 외부에서 임의로 트랜잭션을 시작하지 못하도록 한 것이다.
</div>
</details>
