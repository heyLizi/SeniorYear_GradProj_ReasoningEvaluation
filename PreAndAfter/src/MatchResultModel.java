/*匹配结果的模型*/

public class MatchResultModel {

    private boolean isMatch; //是否匹配
    private String reason;   //原因（若匹配，则原因无意义；若不匹配，则原因为不匹配的原因）

    public MatchResultModel(boolean isMatch, String reason) {
        this.isMatch = isMatch;
        this.reason = reason;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
