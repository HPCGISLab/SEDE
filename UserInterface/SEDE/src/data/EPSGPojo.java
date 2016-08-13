package data;

import java.util.ArrayList;

public class EPSGPojo {
	private boolean exact;
	private String html_terms;
	private ArrayList<CodePojo> codes;
	private boolean html_showResults;
	public boolean isExact() {
		return exact;
	}
	public void setExact(boolean exact) {
		this.exact = exact;
	}
	public String getHtml_terms() {
		return html_terms;
	}
	public void setHtml_terms(String html_terms) {
		this.html_terms = html_terms;
	}
	public ArrayList<CodePojo> getCodes() {
		return codes;
	}
	public void setCodes(ArrayList<CodePojo> codes) {
		this.codes = codes;
	}
	public boolean isHtml_showResults() {
		return html_showResults;
	}
	public void setHtml_showResults(boolean html_showResults) {
		this.html_showResults = html_showResults;
	}
}
