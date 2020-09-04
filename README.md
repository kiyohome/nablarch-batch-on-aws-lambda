# nablarch-batch-on-aws-lambda

NablarchバッチをAWS Lambdaで動かすことを試します。

## S3イベントでFile to Fileバッチ

- SampleBatchクラスがFile to Fileバッチです。入力ファイルの内容を出力ファイルに書き出します。
- NablarchBatchOnS3EventLambdaがAWS Lambdaのエントリとなるクラスです。AWS Labmdaのハンドラインターフェースを実装しています。
- S3FileHandlerクラスがS3⇔ローカルのファイル移動を行います。
  - 既存のFile to Fileバッチのロジックを変更せずに使いまわせるように、ハンドラの最初の方でS3FileHandlerを差し込みます。
  - ローカルのファイルパスはNablarchのファイル管理機能を使っている前提です。
- ログはNablarchのログ出力を標準出力に出してます。標準出力に出すとCloudWatchで見れるようです。
  
DBを使わないように変更しているので、ローカルでのユニットテストはNablarchBatchOnS3EventLambdaTestしか通りません。
SampleBatchのリクエスト単体テストは動かないです。

次のコマンドでAWS Lambdaにデプロイするjarを作ります。
```
mvn -Dmaven.test.skip=true -P prod clean package
```

Lambdaごとに動きを変えるため、環境変数を用意しています。

バッチを起動するための情報を指定します。NablarchBatchOnS3EventLambdaが使用します。

- NABLARCH_DI_CONFIG
  - コンポーネント定義ファイルのパス
  - デフォルト：classpath:batch-boot.xml
- NABLARCH_REQUEST_PATH
  - リクエストパス
  - 必須
  - SampleBatchを実行したい場合；SampleBatch
- NABLARCH_USER_ID
  - ユーザーID
  - デフォルト：batch_user

S3⇔ローカルのファイル移動するための情報を指定できます。S3FileHandlerが使用します。

- NABLARCH_INPUT_BASE_PATH_NAME
  - 入力ファイルのベースパス名
  - デフォルト：input
- NABLARCH_INPUT_FILE_NAME
  - 入力ファイルのファイル名
  - デフォルト：input-data
- NABLARCH_OUTPUT_BASE_PATH_NAME
  - 出力ファイルのベースパス名
  - デフォルト：output
- NABLARCH_OUTPUT_FILE_NAME
  - 出力ファイルのファイル名
  - デフォルト：output-data
- NABLARCH_S3_PUT_BUCKET_NAME
  - 出力ファイルの移動先となるS3のバケット名
  - 必須
  - 無限ループすると困るので明示的に指定してください。入力ファイルのバケットと分けた方がよいと思います。
- NABLARCH_S3_PUT_OBJECT_KEY
  - 出力ファイルを移動した後のS3のオブジェクトキー
  - デフォルト：out-入力ファイルのオブジェクトキー
    - 入力ファイルのオブジェクトキーが"abcd"の場合は"out-abcd"となります。

入力ファイルと出力ファイルを格納するS3のバケットを作ります。

AWSコンソールでLambdaを作ります。

- 基本設定
  - ランタイム：java8
  -ハンドラ；nablarch.integration.amazonaws.lambda.NablarchBatchOnS3EventLambda::handleRequest
- 関数コード
  - mvnコマンドで作成したjarをアップロード
- 環境変数
  - NABLARCH_REQUEST_PATH：SampleBatch
  - NABLARCH_S3_PUT_BUCKET_NAME：作成した出力用のバケット名
  - NABLARCH_INPUT_FILE_NAME：test-input.csv
  - NABLARCH_OUTPUT_FILE_NAME：test-result.csv
- アクセス権限
  - Lambdaに指定したロールにAWSLambdaExecuteポリシー(S3にアクセス可)を付与
  - 参考
    - https://docs.aws.amazon.com/ja_jp/lambda/latest/dg/with-s3-example.html
