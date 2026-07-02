# 使ったプロンプトと評価

演習中に Codex CLI に投げた主要なプロンプトを記録する。
`EXERCISES.html` に記載の機能実装や、修正提案に関するプロンプトを記述する。

## 書き方

- どのフェーズで使ったか (例: 投稿一覧、いいね機能、リファクタ)
- 実際に投げたプロンプト本文 (省略せずコピペ)
- 結果: 効いた / 部分的に効いた / 効かなかった
- 振り返り: 次に同じ状況が来たらどう変えるか

---

## プロンプト 1(サンプル)

**フェーズ**: サンプルデータ(シーダー)の追加

**プロンプト本文**:

```
src\main\resources\db\migration\V1__init.sql に基づくH2用のシーダーを作成し、実行する。テストデータとして10個程度のシードデータを作成。H2で起動時に自動投入する。
```

**結果**: 効いた

```
• 提案どおり、H2 プロファイル起動時だけ seed が自動適用される構成にしました。

  差分:

  - src/main/resources/application-h2.yml:20: H2 の Flyway location に classpath:db/seed/h2 を追加。
  - src/main/resources/db/seed/h2/R__seed_posts_h2.sql:6: posts に 10 件投入する H2 専用 repeatable migration を追加。posts_seq は次
    ID が 11 になるよう更新。

  - src/test/resources/application-h2.yml:17: テストは空 DB 前提を維持するため seed location を外す設定を追加。
  - src/test/java/com/example/tsubuyaki/db/H2SeedMigrationTest.java:18: seed 適用で 10 件入ることと、次 sequence 値が 11 になることを
    検証。

  検証:

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Dtest=H2SeedMigrationTest test 成功
  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-
    application-type=none" 成功。ログで Migrating schema "PUBLIC" with repeatable migration "seed posts h2" を確認。

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 verify 成功。警告: Rule violated for bundle tsubuyaki-board: lines
    covered ratio is 0.48, but expected minimum is 0.60。日本語要約: 現在のカバレッジは 48% で 60% 未満ですが、このプロファイルでは
    fail 設定ではないためビルドは成功しています。
```

**振り返り**:

シーダーは追加できた。
検証方法を指定しなかったためハーネスであらかじめ定義された検証方法を自動的に起動した。
CI検証は成功しているが、実際に画面表示の確認はできていない(未実装のため)。

---

## プロンプト 2

**フェーズ**:
投稿作成＋バリデーション (M2/M3)
**プロンプト本文**:
投稿作成機能（M2/M3）をTDDで実装する。
【仕様】
- GET /posts/new：posts/form.html を表示し、Model に postForm (PostForm) を設定する
- POST /posts：投稿を登録し、created_at は現在日時を設定する
- 成功時は HTTP302 で /posts へリダイレクト
【バリデーション】
- author：必須、1～30文字、空白のみ不可
- body：必須、1～280文字、空白のみ不可
- エラー時は HTTP200 で posts/form.html を再表示し、入力値とエラーメッセージを保持する
【テスト】
以下のテストを先に作成してから実装する。
- GET /posts/new で form が表示され、postForm が Model にあること
- 正常登録で投稿が保存され、/posts にリダイレクトすること
- author、body の必須・文字数・空白のみ入力のバリデーション
- エラー時に form 再表示、入力値保持、エラーメッセージ表示

既存のController / Service / Repository構成に合わせて実装し、最終的にすべてのテストを成功させる。

**結果**: [効いた] / 部分的に効いた / 効かなかった

**振り返り**:
OracleEXでSpringを起動してしまっていたので最初は実装後の動作確認で「投稿」押下後エラーになっていた。
./mvnw -Ph2 spring-boot:run
↑で起動して、画面をctrl+F5したことで実装が正常なことが確認できた。

---

## プロンプト 3

**フェーズ**:
投稿詳細＋ヘルスチェック (M4/M5)
**プロンプト本文**:
【M4: 投稿詳細】
- GET /posts/{id}
- posts/detail.html を表示
- 存在しないidは404
【M5: ヘルスチェック】
- /actuator/health がUPのまま動作すること（実装変更不要）
【制約】
- M2/M3の既存実装は変更しない
- Controller / Service / Repositoryの責務を壊さない
- 仕様外機能は追加しない
【方針】
- EXERCISES.mdの仕様に従うこと
- テストを満たすこと
実装完了後、EXERCISES.mdと照らし合わせてセルフレビューを行い、
仕様漏れ・テスト漏れ・404処理の妥当性を確認してください。

**結果**: [効いた] / 部分的に効いた / 効かなかった

**振り返り**:
URL欄に直接「http://localhost:8080/posts/11」などと入力することで投稿詳細を確認できた。Exercises.mdに投稿詳細のリンクについて明記はしていないが必要だと思うのでこの後実装する。

---

## プロンプト 4 

**フェーズ**:
投稿詳細＋ヘルスチェック (M4/M5)
**プロンプト本文**:

Spring Bootの投稿一覧画面（posts/list.html）に、各投稿から詳細画面へ遷移できるリンクを追加してください。
【要件】
- 各投稿表示ブロックの右下に「詳細」リンクを追加する
- リンク先は GET /posts/{id}
- idは各投稿のIDを使用すること
- Thymeleafのth:hrefを使用すること
【例】
- /posts/1
- /posts/2
【制約】
- Controller / Service / Repositoryは変更しない
- 既存の表示ロジックは壊さない
- M2/M3/M4の仕様は変更しない
- 追加するのは画面リンクのみ
【ゴール】
- 投稿一覧から詳細ページへ遷移できること
- URL直打ちではなく画面から遷移できること

**結果**: [効いた] / 部分的に効いた / 効かなかった

**振り返り**:
投稿表示ブロックの日付のすぐ右側に「詳細」というリンクを作成でき、画面もきちんと遷移できた。問題なし。

---


## プロンプト 5 

**フェーズ**:
MUST達成後の振り返り
**プロンプト本文**:
ありがとう。一度振り返ります。本日実装した内容を完結に説明してください。またなぜ変更が必要だったかについても記載してください。使用したプロンプトとともにまとめてもらえますか。

**結果**: [効いた] / 部分的に効いた / 効かなかった
以下出力内容
本日実装した内容

  1. 投稿作成機能 M2/M3
      - GET /posts/new で新規投稿フォームを表示
      - POST /posts で投稿を登録
      - author / body の必須・文字数・空白のみ NG を検証
      - 成功時は /posts へリダイレクト
      - エラー時は入力値とエラーを保持してフォーム再表示

     必要だった理由:
     EXERCISES.md の M2/M3 で、投稿フォーム表示と投稿登録が MUST 要件だったため。

  2. 投稿詳細機能 M4
      - GET /posts/{id} を追加
      - posts/detail.html を追加
      - 存在する投稿は詳細表示
      - 存在しない id は 404

     必要だった理由:
     EXERCISES.md の M4 で、投稿詳細画面と 404 処理が MUST 要件だったため。

  3. ヘルスチェック M5 の回帰確認
      - /actuator/health が UP を返すテストを追加

     必要だった理由:
     M5 は実装変更不要だが、M4 までの変更でヘルスチェックが壊れていないことを確認する必要があったため。

  4. 一覧から詳細画面へのリンク追加
      - posts/list.html の各投稿に「詳細」リンクを追加
      - リンク先は /posts/{id}
      - th:href で投稿 id を使って URL を生成

     必要だった理由:
     M4 の仕様には明記されていないが、画面操作で詳細画面へ遷移できる導線が必要だったため。URL 直打ちではなく、一覧から自然に詳細へ移
     動できるようにした。

  5. 新規投稿フォームの見た目改善
      - app.css に form[method="post"] 用スタイルを追加
      - ラベルの下に入力欄が来るように調整
      - 入力欄、本文欄、投稿ボタン、エラー表示を見やすくした

     必要だった理由:
     新規投稿画面で「投稿者」「本文」の入力欄の位置が分かりにくく、画面確認・操作性に支障があったため。

  主な変更ファイル

  - PostController.java
      - 投稿作成、投稿詳細の Controller 処理を追加

  - PostService.java
      - 投稿作成、id 検索処理を追加

  - posts/detail.html
      - 投稿詳細画面を追加

  - posts/list.html
      - 各投稿に詳細リンクを追加

  - app.css
      - 詳細リンクの右下配置
      - 新規投稿フォームの見た目改善

  - PostControllerTest.java
      - 投稿作成、バリデーション、詳細表示、404、詳細リンクを検証

  - PostServiceTest.java
      - 投稿作成時の createdAt 設定、id 検索を検証

  - TsubuyakiApplicationTests.java
      - /actuator/health の UP を検証

  使用したプロンプト要約

  - 投稿作成機能 M2/M3 を TDD で実装する
  - EXERCISES.md に従ってセルフレビューする
  - terminal 起動時も H2 で同じ動作確認ができるようにする
  - H2 のデータ閲覧手順を確認する
  - 投稿詳細 M4 とヘルスチェック M5 を実装・確認する
  - 投稿一覧から詳細画面へ遷移できるリンクを追加する
  - 新規投稿画面の入力欄の見た目を改善する

  検証
  最終的に以下を確認済みです。

  ./mvnw -B -Ph2 verify
  BUILD SUCCESS
  Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
  Checkstyle: 0 violations
  SpotBugs: No errors/warnings found
  JaCoCo: All coverage checks have been met

**振り返り**:
振り返りはできた。明日はウォームアップの部分で、ソース比較しながら、さらに理解を深め言語化できるようにしたい。
---



## プロンプト 6 

**フェーズ**:
リファクタ＋カバレッジ80%到達
JaCoCo 80%、-Pcoverage-day3 -Pstrict 緑
**プロンプト本文**:

Spring Bootプロジェクトのテスト改善とリファクタリングを行い、
JaCoCoカバレッジ80%以上および -Pcoverage-day3 -Pstrict が通る状態にしてください。
【ゴール】
- テストカバレッジ80%以上を達成する
- mvn test -Pcoverage-day3 -Pstrict が成功する（GREEN）
- 既存機能（M2〜M4）を壊さない
【対象】
- Controllerテストの不足分を補う
- Serviceテストの不足分を補う
- 投稿詳細（M4）の正常系・異常系テストを補強する
- 404ケースのテストを必ず追加する
【制約】
- プロダクションコード（Controller / Service / Repository）は基本変更しない
- 仕様外機能は追加しない
- リファクタリングは最小限にする（テスト優先）
【観点】
- 正常系テスト
- 異常系テスト（特に存在しないIDの404）
- 境界値（必要な場合のみ）
- MockMvcを適切に使用すること
【追加要件】
- テスト実行後にカバレッジが80%に到達していることを確認する
- すべてのテストがGREENであることを確認する

**結果**: [効いた] / 部分的に効いた / 効かなかった

**振り返り**:
動作確認部分はないが、カバレッジ80％を切っていたファイル・内容はないことが確認できた。
---


## プロンプト 7

**フェーズ**:
いいね機能 (S1)
**プロンプト本文**:

Spring BootでS1（いいね機能）をTDDで実装してください。

【仕様】
- POST /posts/{id}/likes（いいねトグル）
- clientHash（IP+UAをSHA-256→先頭8文字）でユーザー識別
- 同一clientHashで再実行すると解除
- いいね数を保持・表示
- detail画面にいいね数とLikeボタン表示
【TDD手順】
① テスト作成（実装禁止）
- いいね追加
- いいね解除（トグル）
- カウント増減
- 存在しないidは404
- detailにいいね数表示
② 実装（テスト後）
- Controller / Service追加
- 必要ならLikeエンティティ追加
- clientHash生成ロジック実装
③ リファクタ
- Controllerを薄くする
- Service整理
【制約】
- 既存機能変更禁止（M2〜M4）
- 仕様外機能禁止
- アーキテクチャ厳守
【ゴール】
- 全テストGreen
- トグル正常動作
- 404正常

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---


## プロンプト 8

**フェーズ**:
いいね機能 (S1)
**プロンプト本文**:
実装ありがとうございます。
  以下の観点でセルフレビューしてください。

  - S1（いいね機能）の実装漏れはないか
  - POST /posts/{id}/likes が仕様どおり動作するか
  - いいねトグル（同一clientHashで再度押すと解除）は正しく実装されているか
  - clientHash（IP + User-Agent のSHA-256先頭8文字）の生成は仕様どおりか
  - 投稿詳細画面にいいね数とLikeボタンが表示されるか

  問題がなければ、変更したファイルごとに以下を説明してください。

  - 実装内容の概要
  - なぜその変更が必要だったか
  す。

**結果**: [効いた] / 部分的に効いた / 効かなかった
 セルフレビュー結果: S1 は EXERCISES.md の仕様に沿っており、追加修正は不要です。

  確認結果:

  - POST /posts/{id}/likes でいいねトグルを実装済み。
  - 同一 clientHash で再実行すると PostLike を削除し、解除される。
  - clientHash は request.getRemoteAddr() + User-Agent を SHA-256 化し、先頭 8 文字を使用。
  - 投稿詳細画面に いいね数 と Like ボタンを表示。
  - 存在しない投稿 id への Like は 404。
  - 既存の M2/M3/M4 の Controller / Service / Repository 責務は維持。
  - 仕様外の画面や追加機能はなし。POST 後は詳細画面へ戻すため redirect:/posts/{id} にしています。
  - 検証済み: ./mvnw -B -Ph2 verify、./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify ともに BUILD SUCCESS。
  - 現在の JaCoCo 行カバレッジ: 86.75%。

  変更ファイルごとの説明

  - src/main/java/com/example/tsubuyaki/controller/PostController.java
    いいね数を詳細画面の Model に追加し、POST /posts/{id}/likes を追加しました。
    必要だった理由: S1 の「詳細画面でいいね数表示」「Like ボタンからトグル」を Controller 層で受け付けるためです。HTTP 404 への変換も
    ここで行っています。

  - src/main/java/com/example/tsubuyaki/service/LikeService.java
    いいね追加・解除・件数取得の業務処理を追加しました。
    必要だった理由: Controller を薄く保ち、同一 clientHash の存在確認と保存/削除を Service の責務にするためです。

  - src/main/java/com/example/tsubuyaki/service/PostNotFoundException.java
    いいね対象の投稿が存在しない場合の例外を追加しました。
    必要だった理由: Service 層では HTTP を意識せず、Controller 側で 404 に変換できるようにするためです。

  - src/main/java/com/example/tsubuyaki/domain/PostLike.java
    いいねを保持する Entity を追加しました。
    必要だった理由: 投稿ごと・clientHash ごとのいいね状態を DB に保存し、トグル判定できるようにするためです。

  - src/main/java/com/example/tsubuyaki/repository/PostLikeRepository.java
    findByPostIdAndClientHash と countByPostId を追加しました。
    必要だった理由: 同一利用者の既存いいね確認と、詳細画面に出す件数取得に必要なためです。

  - src/main/resources/db/migration/V2__create_post_likes.sql
    post_likes テーブル、シーケンス、一意制約、外部キー、インデックスを追加しました。
    必要だった理由: いいね状態を永続化し、同一投稿・同一 clientHash の重複いいねを DB レベルでも防ぐためです。

  - src/main/resources/templates/posts/detail.html
    いいね数表示と Like ボタンの POST フォームを追加しました。
    必要だった理由: S1 の「詳細画面でいいね数と Like ボタン表示」を満たすためです。

  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java
    詳細画面のいいね数/Likeボタン表示、Like POST、存在しない id の 404 を追加しました。
    必要だった理由: S1 の Controller 観点、画面表示、HTTP 振る舞い、clientHash 生成を検証するためです。

  - src/test/java/com/example/tsubuyaki/service/LikeServiceTest.java
    いいね追加、同一 clientHash による解除、件数取得、存在しない投稿 id の異常系を追加しました。
    必要だった理由: トグルの業務ロジックを Spring 起動なしで直接検証するためです。

  - src/test/java/com/example/tsubuyaki/service/LikeServiceIntegrationTest.java
    H2 上で実際に保存し、カウントが 0 → 1 → 0 と増減することを確認しました。
    必要だった理由: Entity / Repository / Migration を含めた永続化の整合性を確認するためです。

  - src/test/java/com/example/tsubuyaki/sample/SamplePostControllerTest.java
    PostController の依存追加に合わせて LikeService のモックを追加しました。
    必要だった理由: 既存サンプルテストを壊さず、Controller のコンストラクタ変更に追従するためです。
**振り返り**:
よく理解できた
---



## プロンプト 9

**フェーズ**:
キーワード検索 (S2)
**プロンプト本文**:
Spring BootでS2（キーワード検索）をTDDで実装してください。

【仕様】
- GET /posts?q=xxx
- bodyを対象にLIKE検索する
- 一覧画面（posts/list.html）を再利用する
- 一覧画面上部に検索ボックスを追加する
- qが未指定または空の場合は全件表示する
【TDD手順】
① 先にテストを作成する（実装は行わない）
- キーワード検索で一致する投稿のみ表示されること
- 一致しない場合は検索結果が0件になること
- q未指定または空の場合は全件表示されること
- 検索ボックスが表示されること
② テストが失敗することを確認後、実装する
③ テストをすべて成功させ、必要最小限のリファクタリングを行う
【制約】
- EXERCISES.mdの仕様に従うこと
- 既存機能（M2〜M4、S1）は変更しない
- Controller / Service / Repositoryの責務を守る
- 仕様外機能は追加しない
【ゴール】
- すべてのテストがGREENになること
- 検索機能が仕様どおり動作すること

**結果**: [効いた] / 部分的に効いた / 効かなかった

**振り返り**:
仕様どおりに実装できており、最小の変更内容でやってくれていた
---



## プロンプト 10

**フェーズ**:
キーワード検索 (S2)
**プロンプト本文**:
 実装ありがとうございます。
  以下の観点でセルフレビューしてください。

  - S2（キーワード検索）の実装漏れはないか
  - GET /posts?q=xxx が仕様どおり動作するか
  - テスト漏れはないか（検索成功・検索結果0件・q未指定または空・検索フォーム表示）
  - 既存機能（M2〜M4、S1）が壊れていないか
  - 不要な実装や仕様外の実装はないか

  問題があれば修正してください。

  問題がなければ、変更したファイルごとに以下を説明してください。

  - 実装内容の概要
  - なぜその変更が必要だったか

**結果**: [効いた] / 部分的に効いた / 効かなかった
セルフレビュー結果: 問題なし、追加修正は不要です。

  EXERCISES.md の S2 は「GET /posts?q=xxx で本文 LIKE 検索」「一覧画面を再利用」「検索ボックスを上部に追加」です。現在の実装はこの範
  囲に収まっています。

  確認内容:

  - GET /posts?q=xxx は PostController#list で受け取っています。
  - q がある場合は postService.searchByBody(q) を呼びます。
  - Repository は findTop50ByBodyContainingOrderByCreatedAtDesc なので、検索対象は body です。
  - q が未指定または空白のみの場合は postService.latest() に戻り、全件表示です。
  - 表示は既存の posts/list.html を再利用しています。
  - 一覧画面上部に検索フォームを追加済みです。
  - テストは検索成功、検索結果0件、q 空、検索フォーム表示、Service、Repository を追加済みです。
  - ./mvnw -B -Ph2 verify と ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify は成功済みです。
  - カバレッジは 87.06% です。

  変更ファイルごとの説明

  - src/main/java/com/example/tsubuyaki/controller/PostController.java
    q パラメータを受け取り、検索キーワードがある場合は本文検索、ない場合は既存の全件表示に分岐しました。
    必要だった理由: GET /posts?q=xxx と、q 未指定・空の場合の全件表示を Controller で切り替えるためです。

  - src/main/java/com/example/tsubuyaki/service/PostService.java
    searchByBody(String keyword) を追加しました。
    必要だった理由: Controller から Repository を直接呼ばず、既存の Controller / Service / Repository の責務分担を維持するためです。

  - src/main/java/com/example/tsubuyaki/repository/PostRepository.java
    findTop50ByBodyContainingOrderByCreatedAtDesc(String keyword) を追加しました。
    必要だった理由: body を対象に LIKE 検索し、新着順で取得するためです。

  - src/main/resources/templates/posts/list.html
    一覧画面上部に検索フォームを追加しました。
    必要だった理由: URL 直打ちではなく、画面から GET /posts?q=xxx を実行できるようにするためです。

  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java
    検索成功、検索結果0件、q 空文字の全件表示、検索フォーム表示のテストを追加しました。
    必要だった理由: S2 の画面・HTTP・分岐条件を Controller テストで保証するためです。

  - src/test/java/com/example/tsubuyaki/service/PostServiceTest.java
    searchByBody が Repository の本文検索へ委譲するテストを追加しました。
    必要だった理由: Service 層の責務が崩れていないことを確認するためです。

  - src/test/java/com/example/tsubuyaki/repository/PostRepositoryTest.java
    本文にキーワードを含む投稿のみ返すこと、一致しない場合に空になることを追加しました。
    必要だった理由: 実際の JPA クエリが body LIKE 検索として動くことを確認するためです。

**振り返り**:
よく理解できた。やはりこのプロンプトをフェーズごとに入れると翌日のソースリーディングに役立つ。
---



## プロンプト 11

**フェーズ**:
投稿者拡張＋UX微調整 (S3)
**プロンプト本文**:

Spring BootでS3（投稿者名フィールド拡張）をTDDで実装してください。

【仕様】
- 投稿者名は必須
- 投稿フォームでアバター色を選択できるようにする（簡易）
- 選択したアバター色を投稿に保存する
- 投稿一覧・投稿詳細でアバター色を表示する
- アバター色の選択方法・色の種類は、仕様外にならない範囲で最もシンプルな実装とする
【前提】
- 投稿者名の必須バリデーション（PostFormの@NotBlank）は既に実装済みです。
- 既存のバリデーションは変更・重複実装しないでください。
【TDD手順】
① 先にテストを作成する（実装は行わない）
- 投稿フォームにアバター色選択欄が表示されること
- アバター色が保存されること
- 投稿一覧でアバター色が表示されること
- 投稿詳細でアバター色が表示されること
② テストが失敗することを確認した後、実装する
③ テストをすべて成功させ、必要最小限のリファクタリングを行う
【制約】
- EXERCISES.mdの仕様に従うこと
- 既存機能（M2〜M4、S1、S2）は変更しない
- Controller / Service / Repository の責務を守る
- 仕様外機能は追加しない
【ゴール】
- すべてのテストがGREENになること
- アバター色が保存・表示されること
- 既存機能が正常に動作すること
実装完了後は、EXERCISES.mdと照らし合わせてセルフレビューを行い、仕様漏れ・テスト漏れ・既存機能への影響がないことを確認してください。


**結果**: [効いた] / 部分的に効いた / 効かなかった

**振り返り**:

---