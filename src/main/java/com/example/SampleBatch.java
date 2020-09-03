package com.example;

import nablarch.common.code.CodeUtil;
import nablarch.common.io.FileRecordWriterHolder;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.message.MessageUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Result;
import nablarch.fw.Result.Success;
import nablarch.fw.action.NoInputDataBatchAction;

import java.util.HashMap;
import java.util.Map;

/**
 * 疎通確認用の都度起動バッチアクションクラス。
 * <p>
 * 以下の機能について、疎通確認を行う。
 * <ul>
 * <li>ディスパッチ機能</li>
 * <li>データベースアクセス</li>
 * <li>メッセージ機能</li>
 * <li>コード機能</li>
 * </ul>
 * <p>
 * 疎通確認に失敗した場合は、その時点で例外が発生する。
 * </p>
 * <p>
 * 全ての疎通確認に成功した場合、フォーマット定義（EXAMPLE.fmt）を使用して、
 * 各機能の疎通結果がファイル出力される(test-result.csv)。
 * <p>
 * <pre>
 * 【出力例】
 * "dispatch", "OK"
 * "database","OK"
 * "message","OK"
 * "code","OK"
 * "thread context","OK"
 * </pre>
 * </p>
 *
 * @deprecated TODO 疎通確認完了後、削除して下さい。
 */
public class SampleBatch extends NoInputDataBatchAction {

    /**
     * ロガー。
     */
    private static final Logger LOGGER = LoggerManager.get(SampleBatch.class);

    /**
     * 出力ファイル。
     */
    private static final String OUTPUT_FILE_NAME = "test-result.csv";

    /**
     * レイアウトファイル。
     */
    private static final String LAYOUT_FILE_NAME = "EXAMPLE";

    /**
     * 疎通確認用に使用するメッセージID。
     */
    private static final String MESSAGE_ID = "sample.error.message";

    /**
     * 疎通確認用に使用するコードID。
     */
    private static final String CODE_ID = "C0000001";

    /**
     * 疎通確認用に使用するコード値。
     */
    private static final String CODE_VALUE = "1";


    /**
     * {@inheritDoc}
     */
    @Override
    public Result handle(ExecutionContext ctx) {

        LOGGER.logInfo("疎通確認を開始します。");

        FileRecordWriterHolder.open(OUTPUT_FILE_NAME, LAYOUT_FILE_NAME);

        // ディスパッチ機能の疎通確認
        // (このメソッドが呼ばれたということはディスパッチ機能は機能している)
        writeOkRecord("dispatch");

        // メッセージ機能の疎通確認
        checkMessageFunction();
        writeOkRecord("message");

        // コード機能の疎通確認
        checkCodeFunction();
        writeOkRecord("code");

        LOGGER.logInfo("疎通確認が完了しました。");

        return new Success("疎通確認が完了しました。");
    }

    /**
     * メッセージ機能の疎通確認を行う。
     */
    private void checkMessageFunction() {
        try {
            MessageUtil.getStringResource(MESSAGE_ID);
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "メッセージ機能の疎通確認に失敗しました。ネストした例外メッセージを確認して下さい。", e);
        }
    }

    /**
     * コード機能の疎通確認を行う。
     */
    private void checkCodeFunction() {
        try {
            String codeName = CodeUtil.getName(CODE_ID, CODE_VALUE);
            LOGGER.logInfo("取得したコード名称：" + codeName);
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "コード機能の疎通確認に失敗しました。ネストした例外メッセージを確認して下さい。", e);
        }
    }

    /**
     * レコードを書き込む。
     *
     * @param okFunctionName 疎通確認OKであった機能名
     */
    private void writeOkRecord(String okFunctionName) {
        Map<String, Object> record = new HashMap<String, Object>();
        record.put("functionName", okFunctionName);
        record.put("result", "OK");
        FileRecordWriterHolder.write(record, OUTPUT_FILE_NAME);
    }
}
