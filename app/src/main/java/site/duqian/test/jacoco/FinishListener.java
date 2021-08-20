package site.duqian.test.jacoco;


public interface FinishListener {
    void onActivityFinished();
    void dumpIntermediateCoverage(String filePath);
}
