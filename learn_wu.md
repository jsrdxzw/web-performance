R-ISUCON初挑戦で、やはり大変で難しかったです。<br>
**もっとできたはずだ**というのは最終的な感想で、悔しかったです。

でも、学びも多いので、ここで簡単にまとめます。

# 目次
1. [タスク](#task)
2. [流れ](#flow)
3. [学び（仕事編）](#learn_work)
4. [学び（技術編）](#learn_tech)
5. [まとめ](#conclusion)

# 内容

## タスク<a name="task"></a>

R-ISUCONはざっくりいうと

    与えられたwebsiteを高速化して、ベンチマークにかけて、点数の高いチームが勝ち。

というハッカソンです。

今回与えられたwebsiteは勉強会の検索予約websiteです。<br>

機能： 

* 一覧 
* ログイン 
* 予約、QRコード生成
* 諸々の検索

環境： 

* AWSのEC2インスタンス三台、単独でサイトが立ち上がる状態
* spring boot + MySQL5.7

スケジュール：

7/19午後15時スタート、7/20お昼12時終了

## 流れ<a name="flow"></a>

事前準備：

    チームメンバーが事前にフロントからバックエンドまでの最適化案を考えてくれたが、
    今回のタスクにおいてはほとんどやくに立たなかった

チューニング：

* confを変える
* テーブル設計を見直す
* SQLをチューニング

結果：

    スコア1500でスタートして、1800で終わった（最高得点は14万）

## 学び（仕事編）<a name="learn_work"></a>

### 最大の失敗

今回<u>一番失敗した</u>ところは、事前準備にしたがってチューニングを行うことにしてしまったところです。<br>

<center><strong>サイトログ分析→問題特定→解決案を絞りだす</strong></center>

という*当たり前*を実行しなかったところにあると思います。<br>
まさに**推測より、測定せよ**という法則を無視した結果です。

事前準備段階では
* Java8 -Java11へのアップグレード
* Http1.1 -Http2.0へのアップグレード
* フロント側の圧縮

などが、コンテストでは不要または実行不可能になっていました。<br>
本当のボトルネックであるDB設計とSQLにたどり着くまで半分以上の時間をかかってしまいました。

### 他の教訓

1. マイルストーンの設定
    今までのisucon参加者の経験を事前準備で重要視しなかったのも一つの原因ですが、
    最大の問題点は<u>限られた時間内で、マイルストーンの設定をしなかった</u>ところです。<br>
    最初の段階で開発態勢を整えるべく、gitとか入れたりしたのですが、jarファイルのデプロイが
    手動で行う必要があることに気づいたのは夜中の1時くらいでした。つまり、
    <center>「開発態勢を整えた」は何をもって完了したと言えるか</center>
    をそもそも定義しなかったです。

 2. 先入観を捨て、合理性に基づいて施策する
    前職の経験でテーブル設計では正規化の重要性が叩き込まれ、テーブル設計を変更することは慎重に行うべきだという先入観がありました。<br>
    しかし、isuconのタスクは高速化することが目的なので、非正規化は合理的です。


## 学び（技術編）<a name="learn_tech"></a>
1. Mysqlのslow-logによる遅いqueryの特定<br>
    手順：
    1. my.cnf設定ファイルの編集
    ```
    slow_query_log                = 1
    slow_query_log_file           = /var/lib/mysql/mysqld-slow.log
    long_query_time               = 0
    log-queries-not-using-indexes = 1
    ```
    2. MySQLを再起動<br>
    `sudo systemctl mysql restart`
    3. ベンチマーク実行して、分析ツールでレポート生成する<br>
    分析ツール準備<br>
    ```
    wget percona.com/get/pt-query-digest
    chmod u+x pt-query-digest
    ```
    ログ解析<br>
    `sudo ./pt-query-digest /var/lib/mysql/mysqld-slow.log`<br>
    下記ののようなログが出ます
    ```
    # Query 1: 0.55 QPS, 0.70x concurrency, ID 0x91597391CFEA84BF87B7AA8B25513B61 at byte 19168008
    # Scores: V/M = 0.38
    # Time range: 2019-07-19T12:34:09 to 2019-07-19T12:37:41
    # Attribute    pct   total     min     max     avg     95%  stddev  median
    # ============ === ======= ======= ======= ======= ======= ======= =======
    # Count          0     117
    # Exec time     43    149s   139ms      3s      1s      2s   698ms      1s
    # Lock time      0     8ms    37us   955us    70us    84us    83us    57us
    # Rows sent      7   2.29k      20      20      20      20       0      20
    # Rows examine  43  20.73M 180.49k 184.55k 181.40k 182.98k   2.03k 174.27k
    # Query size     0  53.23k     464     468  465.91  463.90       0  463.90
    # String:
    # Databases    ronnpass
    # Hosts        localhost
    # Users        isucon
    # Query_time distribution
    #   1us
    #  10us
    # 100us
    #   1ms
    #  10ms
    # 100ms  #################################################
    #    1s  ################################################################
    #  10s+
    # Tables
    #    SHOW TABLE STATUS FROM `ronnpass` LIKE 'events'\G
    #    SHOW CREATE TABLE `ronnpass`.`events`\G
    #    SHOW TABLE STATUS FROM `ronnpass` LIKE 'users'\G
    #    SHOW CREATE TABLE `ronnpass`.`users`\G
    #    SHOW TABLE STATUS FROM `ronnpass` LIKE 'plans'\G
    #    SHOW CREATE TABLE `ronnpass`.`plans`\G
    #    SHOW TABLE STATUS FROM `ronnpass` LIKE 'reservations'\G
    #    SHOW CREATE TABLE `ronnpass`.`reservations`\G
    # EXPLAIN /*!50100 PARTITIONS*/
    SELECT events.id FROM events INNER JOIN users ON events.owner_username = users.username INNER JOIN plans ON users.plan_id = plans.id LEFT OUTER JOIN (SELECT COUNT(1) AS reservation_num, event_id FROM reservations GROUP BY event_id) res ON events.id = res.event_id WHERE date '2019-07-18 12:34:31.933373' ORDER BY YEAR(events.date) ASC, WEEK(events.date, 1) ASC, plans.rating DESC, COALESCE(reservation_num, 0)/capacity ASC, events.created_at ASC LIMIT 20 OFFSET 380\G
    ```
2. SQLをチューニング＆DB設計見直す
    １で特定したqueryが悪さをしているので、それを改善する<br>
    問題を分解すると
        >(1) `join`が多すぎて<br>
        → 非正規化する
        >(2) ソートカラムが多く、かつindexがない<br>
        → `virtual column`を作って、indexをはる<br>
        → 複数カラムindexを作る
        >(3) `where`文でほぼ全てのqueryで本日以降の条件が入る<br>
        → 過去データを別テーブルに退避させる
        >(4) `like`検索が２カラムで行われている<br>
        → `ngram`や全文検索エンジンを導入する

3. インフラ周り
    * 三台サーバーのハードウェアが違った<br>
    * DBサーバー、APサーバーを分けて、LBを入れる

4. ベンチマーク対策
    * zero time cache pattern：
    > 同じ結果を返す処理が並行に呼び出された場合に、1回の処理にまとめて相乗りさせるような仕組みを提供します。

# まとめ<a name="conclusion"></a>

COLONY箱根に二日間集中的にタスクをやるというようなハッカソンは個人的に初体験です。エンジニア文化を体感して、チームメンバーや他のチームの方々の熱意をすごく感じました。自分のチームも含めて、徹夜したチームは多かったです。限界まで挑戦し続ける精神はさすがだなと思いました。<br>
生活面では温泉、食事、設備など十分すぎるほど充実していました。実りの多い二日間でした。来年もし機会があれば、リベンジしたいと思います。