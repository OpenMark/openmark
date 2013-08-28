package om.administration.questionbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClearanceResponse {

	private Map<String, IdentifiedSuperfluousQuestion> superfluousQuestions
		= new HashMap<String, IdentifiedSuperfluousQuestion>();

	private List<TestSynchronizationCheck> outOfSyncTests
		= new ArrayList<TestSynchronizationCheck>();

	private List<BrokenTestQuestionReferences> brokenTests;
	
	private List<BrokenTestXML> brokenTestXML = new ArrayList<BrokenTestXML>();
	
	private List<BrokenTestXML> orphanTestXML = new ArrayList<BrokenTestXML>();

	
	private Map<IdentifiedSuperfluousQuestion, RemovalIssueDetails> problemRemoving
		= new HashMap<IdentifiedSuperfluousQuestion, RemovalIssueDetails>();
	

	private List<String> archiveDirs= new ArrayList<String>();
	
	
	public List<String> getArchiveDirs() {
		return archiveDirs;
	}
	
	public String getArchiveDir(int i) {
		return archiveDirs.get(i);
	}

	public String getArchiveDirsAsString() {
		String archiveDir=null;
		if (this.archiveDirs.size() >0 ){
			archiveDir=this.archiveDirs.get(0);
			for (int i = 1; i < this.archiveDirs.size(); i++) {
				archiveDir=archiveDir+",";
				archiveDir=archiveDir + this.archiveDirs.get(i);
			}
		}
		return archiveDir;
	}
 
	public void addUniqArchiveDir(String archiveDir) {
		if (null != archiveDir && !this.archiveDirs.contains(archiveDir)) {		
			this.archiveDirs.add(archiveDir);
		}
	}
	public List<BrokenTestXML> getBrokenTestXML() {
		return brokenTestXML;
	}

	public List<BrokenTestXML> getOrphanTestXML() {
		return orphanTestXML;
	} 
	
	public void addBrokenTestXML(BrokenTestXML broken) {
		if (null != broken) {
			getBrokenTestXML().add(broken);
		}
	}

	public void addOrphanTestXML(BrokenTestXML orphan) {
		if (null != orphan) {
			getOrphanTestXML().add(orphan);
		}
	}
	public List<BrokenTestQuestionReferences> getBrokenTests() {
		if (null == brokenTests) {
			brokenTests = new ArrayList<BrokenTestQuestionReferences>(); 
		}
		return brokenTests;
	}

	public void addBrokenTest(Set<BrokenTestQuestionReferences> itqr) {
		if (null != itqr ? itqr.size() > 0 : false) {
			getBrokenTests().addAll(itqr);
		}
	}

	public boolean hasSupersluousQuestions() {
		return null != superfluousQuestions ? superfluousQuestions.size() > 0 : false;
	}

	public void addSuperfluousQuestion(IdentifiedSuperfluousQuestion q) {
		if (null != q ? null != q.getName() ? q.getName().length() > 0 : false : false) {
			superfluousQuestions.put(q.getName(), q);
		}
	}

	public void addOutOfSyncTest(TestSynchronizationCheck check) {
		if (null != check) {
			outOfSyncTests.add(check);
		}
	}

	public void addProblemRemoving(IdentifiedSuperfluousQuestion q,
		RemovalIssueDetails rid) {
		if (null != q && null != rid) {
			problemRemoving.put(q, rid);
		}
	}

	public Map<String, IdentifiedSuperfluousQuestion> getSuperfluousQuestions() {
		return Collections.unmodifiableMap(superfluousQuestions);
	}

	public List<TestSynchronizationCheck> getOutOfSyncTests() {
		return Collections.unmodifiableList(outOfSyncTests);
	}

	public Map<IdentifiedSuperfluousQuestion, RemovalIssueDetails> getProblemRemoving() {
		return Collections.unmodifiableMap(problemRemoving);
	}

}
