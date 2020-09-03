-- バッチ処理対象データを取得するSQL
GET_BATCH_INPUT_DATA =
SELECT
    USER_INFO_ID,
    LOGIN_ID,
    KANJI_NAME,
    KANA_NAME
FROM
    SAMPLE_USER
WHERE
    STATUS = '0'      -- 未処理のレコードが対象
ORDER BY
    USER_INFO_ID

-- 処理ステータスを'1'(処理済み)に更新するSQL
UPDATE_STATUS_NORMAL_END =
UPDATE
    SAMPLE_USER
SET
    STATUS = '1'
WHERE
    USER_INFO_ID = :userInfoId

-- 処理ステータスを'2'(エラー)に更新するSQL
UPDATE_STATUS_ABNORMAL_END =
UPDATE
    SAMPLE_USER
SET
    STATUS = '2'
WHERE
    USER_INFO_ID = :userInfoId
