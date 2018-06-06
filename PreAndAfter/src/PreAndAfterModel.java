/*前后件分离结果的模型*/

public class PreAndAfterModel {

	private String partNo;	    //编序号
	private String chapterNo;   //章序号
	private String sectionNo;   //节序号
	private String articleNo;   //条序号
	private String paragraphNo;  //款序号
	private String subParagraphNo; //项序号，可以为空
	private String antecedent; //前件
	private String consequent; //后件

	public PreAndAfterModel() {
		this.antecedent = "";
		this.consequent = "";
	}

	public PreAndAfterModel(String antecedent, String consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}
	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}

	public String getChapterNo() {
		return chapterNo;
	}

	public void setChapterNo(String chapterNo) {
		this.chapterNo = chapterNo;
	}

	public String getSectionNo() {
		return sectionNo;
	}

	public void setSectionNo(String sectionNo) {
		this.sectionNo = sectionNo;
	}

	public String getArticleNo() {
		return articleNo;
	}

	public void setArticleNo(String articleNo) {
		this.articleNo = articleNo;
	}

	public String getParagraphN() {
		return paragraphNo;
	}

	public void setParagraphN(String paragraphN) {
		this.paragraphNo = paragraphN;
	}

	public String getSubParagraphNo() {
		return subParagraphNo;
	}

	public void setSubParagraphNo(String subParagraphNo) {
		this.subParagraphNo = subParagraphNo;
	}

	public String getAntecedent() {
		return antecedent;
	}
	public void setAntecedent(String antecedent) {
		this.antecedent = antecedent;
	}

	public String getConsequent() {
		return consequent;
	}
	public void setConsequent(String consequent) {
		this.consequent = consequent;
	}

}
