package com.example;

import java.util.HashMap;
import java.util.Map;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.Result.Success;
import nablarch.fw.action.BatchAction;
import nablarch.fw.reader.DatabaseRecordReader;
import nablarch.fw.reader.DatabaseTableQueueReader;

/**
 * 疎通確認用の常駐バッチアクションクラス。
 *
 * @deprecated TODO 疎通確認完了後、削除して下さい。
 */
public class SampleResiBatch extends BatchAction<Map<String, Object>> {

    /** ロガー。*/
    private static final Logger LOGGER
            = LoggerManager.get(SampleResiBatch.class);

    /** データが存在しないときの待機時間（ミリ秒）。*/
    private static final int WAIT_TIME = 10000;

    /** {@inheritDoc} */
    @Override
    public Result handle(Map<String, Object> inputData, ExecutionContext ctx) {

        LOGGER.logInfo("handleが呼ばれました。");
        LOGGER.logInfo("USER_INFO_ID:" + inputData.get("USER_INFO_ID"));
        LOGGER.logInfo("LOGIN_ID:" + inputData.get("LOGIN_ID"));
        LOGGER.logInfo("KANA_NAME:" + inputData.get("KANA_NAME"));
        LOGGER.logInfo("KANJI_NAME:" + inputData.get("KANJI_NAME"));

        return new Success("handleの呼び出しに成功");
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 正常終了時に呼び出される。<br />
     * 処理したレコードの処理ステータスを、正常終了に更新する。<br />
     */
    @Override
    protected void transactionSuccess(Map<String, Object> inputData,
            ExecutionContext context) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userInfoId", inputData.get("userInfoId"));

        getParameterizedSqlStatement("UPDATE_STATUS_NORMAL_END")
                .executeUpdateByMap(map);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 異常終了時に呼び出される。<br />
     * 処理したレコードの処理ステータスを、異常終了に更新する。<br />
     */
    @Override
    protected void transactionFailure(Map<String, Object> inputData,
            ExecutionContext context) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userInfoId", inputData.get("userInfoId"));

        getParameterizedSqlStatement("UPDATE_STATUS_ABNORMAL_END").
                executeUpdateByMap(map);
    }

    /**
     * {@inheritDoc}
     * 入力データを取得するための{@link DatabaseRecordReader}を生成する。
     * 入力データは、処理ステータスが未処理のデータのみを対象とする。
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public DataReader createReader(ExecutionContext ctx) {
        // データベースレコードリーダを生成
        DatabaseRecordReader reader = new DatabaseRecordReader();
        reader.setStatement(getSqlPStatement("GET_BATCH_INPUT_DATA"));

        return new DatabaseTableQueueReader(reader, WAIT_TIME, "USER_INFO_ID");
    }

}
