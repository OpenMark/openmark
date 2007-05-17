/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package om.loadtest;

import java.io.File;
import java.net.URL;
import java.util.*;

import om.axis.qengine.OmServiceServiceLocator;
import util.misc.*;

/**
 * Class for load-testing a question engine.
 */
public class QETester
{
	// Test configuration parameters
	////////////////////////////////
	
	/** Service URL - if null, runs locally inside IDE instead of via web service */
	private final static String SERVICEURL="http://sparrow.open.ac.uk/om-qe/services/Om";
//	private final static String SERVICEURL="http://localhost:8080/om-qe/services/Om";
//	private final static String SERVICEURL=null;	
	
	/** Random thread delays */
	private final static int DELAYMIN=0,DELAYMAX=0;
	
	/** Random question selection (inclusive range; 
	 *  0 = samples.q1, 4=samples.q5, 5=graph 6=audio 7=jme 
	 *  8 = graph without sin calculations */
	private final static int QUESTIONMIN=5,QUESTIONMAX=5;
	
	/** Number of iterations per thread */
	private final static int ITERATIONS=100;
	
	/** Number of threads */
	private final static int THREADS=1;
	
	/** If true, runs only 1 iteration and outputs results */
	private final static boolean DEBUG=false;
	
	/** 
	 * If true, only completes start() and first process() in each question then 
	 * gives up, leaving the session hanging. Then lurks around until you kill
	 * the program, display engine info every few seconds.
	 */
	private final static boolean LEAVESESSIONS=false;
	
	/**
	 * Run the test.
	 * @param args Not used.
	 */
	public static void main(String[] args)
	{
		try
		{
			new QETester();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	static class Step
	{
		int iSeq;
		String sName,sValue;
		
		Step(int iSeq,String sName,String sValue)
		{
			this.iSeq=iSeq; this.sName=sName; this.sValue=sValue;
		}
	}
	
	static class QuestionSteps
	{
		String sID;
		Step[] asSteps;
		int iMajor,iMinor;
		long lRSeed;
		
		QuestionSteps(String sID,int iMajor,int iMinor,long lRSeed,Step[] asSteps)
		{
			this.sID=sID;
			this.asSteps=asSteps;
			this.iMajor=iMajor;
			this.iMinor=iMinor;
			this.lRSeed=lRSeed;
		}
	}
	
	/** Rseed from database also */
	private final static long TESTRSEED1=1121953794043L,TESTRSEED2=1122029257610L;

	/** Steps from database, converted using process above */
	private final static QuestionSteps[] QUESTIONSTEPS=
	{		
		new QuestionSteps("samples.q1",1,1,TESTRSEED1,new Step[]{  
			new Step(1,"omact_gen_0","Submit"),
			new Step(1,"omval_input","3"),
			new Step(2,"omact_ok","OK"),
			new Step(3,"omact_gen_0","Submit"),
			new Step(3,"omval_input","3"),
			new Step(4,"omact_ok","OK"),
			new Step(5,"omact_gen_0","Submit"),
			new Step(5,"omval_input","-3"),
			new Step(6,"omact_next","Next"),
		}),
		new QuestionSteps("samples.q2",1,0,TESTRSEED1,new Step[]{  
			new Step(1,"omact_gen_3","Submit"),
			new Step(1,"omval_denominator","14"),
			new Step(1,"omval_numerator","1"),
			new Step(2,"omact_gen_5","OK"),
			new Step(3,"omact_gen_3","Submit"),
			new Step(3,"omval_denominator","14"),
			new Step(3,"omval_numerator","1"),
			new Step(4,"omact_gen_5","OK"),
			new Step(5,"omact_gen_3","Submit"),
			new Step(5,"omval_denominator","14"),
			new Step(5,"omval_numerator","9"),
			new Step(6,"omact_gen_5","OK"),
		}),
		new QuestionSteps("samples.q3",1,0,TESTRSEED1,new Step[]{  
			new Step(1,"omact_gen_0","Submit"),
			new Step(1,"omval_input","5.687x10<sup>1</sup>"),
			new Step(2,"omact_gen_2","OK"),
			new Step(2,"omval_input","5.687x10<sup>1</sup>"),
			new Step(3,"omact_gen_0","Submit"),
			new Step(3,"omval_input","5.687x10<sup>1</sup>"),
			new Step(4,"omact_gen_2","OK"),
			new Step(4,"omval_input","5.687x10<sup>1</sup>"),
			new Step(5,"omact_gen_0","Submit"),
			new Step(5,"omval_input","5.687x10<sup>2</sup>"),
			new Step(6,"omact_gen_2","OK"),
			new Step(6,"omval_input","5.687x10<sup>2</sup>"),
		}),
		new QuestionSteps("samples.q4",1,1,TESTRSEED1,new Step[]{  
			new Step(1,"omact_gen_8","Submit"),
			new Step(1,"omval_box3","on"),
			new Step(1,"omval_box4","on"),
			new Step(1,"omval_box5","on"),
			new Step(1,"omval_box6","on"),
			new Step(1,"omval_box7","on"),
			new Step(2,"omact_ok","OK"),
			new Step(3,"omact_gen_8","Submit"),
			new Step(3,"omval_box3","on"),
			new Step(3,"omval_box4","on"),
			new Step(3,"omval_box5","on"),
			new Step(3,"omval_box6","on"),
			new Step(3,"omval_box7","on"),
			new Step(4,"omact_ok","OK"),
			new Step(5,"omact_gen_8","Submit"),
			new Step(5,"omval_box1","on"),
			new Step(5,"omval_box2","on"),
			new Step(5,"omval_box3","on"),
			new Step(5,"omval_box5","on"),
			new Step(5,"omval_box6","on"),
			new Step(6,"omact_next","Next"),
		}),
		new QuestionSteps("samples.q5",1,0,TESTRSEED1,new Step[]{  
			new Step(1,"omact_gen_0","Submit"),
			new Step(1,"omval_gradient","a3"),
			new Step(1,"omval_intercept","a2"),
			new Step(2,"omact_gen_3","OK"),
			new Step(2,"omval_gradient","a3"),
			new Step(2,"omval_intercept","a2"),
			new Step(3,"omact_gen_0","Submit"),
			new Step(3,"omval_gradient","a3"),
			new Step(3,"omval_intercept","a2"),
			new Step(4,"omact_gen_3","OK"),
			new Step(4,"omval_gradient","a3"),
			new Step(4,"omval_intercept","a2"),
			new Step(5,"omact_gen_0","Submit"),
			new Step(5,"omval_gradient","a2"),
			new Step(5,"omval_intercept","a5"),
			new Step(6,"omact_gen_3","OK"),
			new Step(6,"omval_gradient","a2"),
			new Step(6,"omval_intercept","a5"),
		}),
		new QuestionSteps("samples.testgraph",1,0,TESTRSEED2,new Step[]{
			new Step(1,"omact_gen_0","Submit"),
			new Step(1,"omval_amplitude","2"),
			new Step(1,"omval_wavelength","3"),
			new Step(2,"NULL","NULL"),
			//new Step(2,"omact_gen_2","OK"),
			new Step(3,"omact_gen_0","Submit"),
			new Step(3,"omval_amplitude","2"),
			new Step(3,"omval_wavelength","3"),
			new Step(4,"omact_gen_2","OK"),
			new Step(5,"omact_gen_0","Submit"),
			new Step(5,"omval_amplitude","1"),
			new Step(5,"omval_wavelength","6"),
			new Step(6,"omact_gen_2","OK"),
		}),
		new QuestionSteps("samples.testaudio",1,0,TESTRSEED2,new Step[]{
			new Step(1,"omact_hereR","Here"),
			new Step(2,"omact_hereL","Here"),
			new Step(3,"omact_hereP","Here"),
			new Step(4,"omact_hereK","Here"),
			new Step(5,"omact_gen_1","OK"),
		}),
		new QuestionSteps("samples.testjme",1,0,TESTRSEED2,new Step[]{
			new Step(1,"omact_myjme","submit"),
			new Step(1,"omval_myjme","CC(C)C"),
			new Step(2,"omact_ok","OK"),
			new Step(2,"omval_myjme",""),
			new Step(3,"omact_myjme","submit"),
			new Step(3,"omval_myjme","CC(C)C"),
			new Step(4,"omact_ok","OK"),
			new Step(4,"omval_myjme",""),
			new Step(5,"omact_myjme","submit"),
			new Step(5,"omval_myjme","CC(C)C(C)(C)C"),
			new Step(6,"omact_next","Next"),
			new Step(6,"omval_myjme",""),
		}),
		new QuestionSteps("samples.testgraphx",1,1,TESTRSEED2,new Step[]{
			new Step(1,"omact_gen_0","Submit"),
			new Step(1,"omval_amplitude","2"),
			new Step(1,"omval_wavelength","3"),
			new Step(2,"omact_ok","OK"),
			new Step(3,"omact_gen_0","Submit"),
			new Step(3,"omval_amplitude","2"),
			new Step(3,"omval_wavelength","3"),
			new Step(4,"omact_ok","OK"),
			new Step(5,"omact_gen_0","Submit"),
			new Step(5,"omval_amplitude","1"),
			new Step(5,"omval_wavelength","6"),
			new Step(6,"omact_ok","OK"),
		}),
	};

	/** Service reference */
	private om.axis.qengine.OmService osRemote;
	private om.qengine.OmService osLocal;
	
	/** Time statistics */
	private TimeStatistics 
		tsStart=new TimeStatistics(),
		tsProcess=new TimeStatistics();
	
	/** Track how many threads have finished */
	private int iFinished=0;
	
	private QETester() throws Exception
	{
		// Get service
		if(SERVICEURL!=null)
		{
			OmServiceServiceLocator ossl=new OmServiceServiceLocator();
			osRemote=ossl.getOm(new URL(SERVICEURL));
		}
		else
		{
			osLocal=new om.qengine.OmService();
			osLocal.init(new TestServletEndpointContext(new TestServletContext(
				new File("//sparrow/om-qe")
				)));		
		}
		
		// Do test iteration of each question then clear statistics
		System.out.println("Initial question preload...");
		for(int iQuestion=QUESTIONMIN;iQuestion<=QUESTIONMAX;iQuestion++)
			doIteration(null,QUESTIONSTEPS[iQuestion]);
		tsProcess=new TimeStatistics();
		tsStart=new TimeStatistics();		
		
		// Start threads
		System.out.println("\nMeasured test:");
		int iActualThreads=DEBUG ? 1 : THREADS;
		for(int i=0;i<iActualThreads;i++) 	new TestThread();
		while(iFinished<iActualThreads)
		{
			synchronized(this)
			{
				wait();
			}
		}
		System.out.println("\tMean\tMin\t5%\t10%\t15%\t20%\t25%\t30%\t35%\t40%\t45%\tMedian\t55%\t60%\t65%\t70%\t75%\t80%\t85%\t90%\t95%\tMax");
		System.out.println("start()\t"+tsStart);
		System.out.println("process()\t"+tsProcess);
		
		while(true)
		{
			System.out.println();
			if(osRemote!=null)
				System.out.println(osRemote.getEngineInfo());
			else
				System.out.println(osLocal.getEngineInfo());				
			if(!LEAVESESSIONS) break;
			Thread.sleep(10000);
		}
		
		if(SERVICEURL==null)
			osLocal.destroy();
	}
	
	class TestThread extends Thread
	{
		TestThread()
		{
			start();
		}
		@Override
		public void run()
		{
			Random r=new Random(System.currentTimeMillis() ^ this.hashCode());
			
			for(int i=0;i<(DEBUG ? 1 : ITERATIONS);i++)
			{			
				// Pick question
				QuestionSteps qsQuestion=QUESTIONSTEPS[
				  r.nextInt(QUESTIONMAX-QUESTIONMIN+1)+QUESTIONMIN];
				
				// Do it
				doIteration(r,qsQuestion);				
			}
			synchronized(QETester.this)				
			{
				iFinished++;
				QETester.this.notifyAll();
			}
		}
	}

	private void doIteration(Random r,QuestionSteps qsQuestion)
	{
		List<String> lThings=new LinkedList<String>();
		
		lThings.add(qsQuestion.sID);
		
		try
		{
			// Delay
			if(r!=null) Thread.sleep(r.nextInt(DELAYMAX-DELAYMIN+1)+DELAYMIN);
			
			// Init question
			NameValuePairs nvp=new NameValuePairs();
			nvp.add("randomseed",""+(qsQuestion.lRSeed+1));
			String sSession;
			long lBefore=System.currentTimeMillis();
			if(osRemote!=null)
			{
				om.axis.qengine.StartReturn sr=osRemote.start(qsQuestion.sID,
					qsQuestion.iMajor+"."+qsQuestion.iMinor,
					"frog",nvp.getNames(),nvp.getValues(),null);
				if(DEBUG)
					System.err.println(sr.getXHTML());
				sSession=sr.getQuestionSession();
			}
			else
			{
				om.qengine.StartReturn sr=osLocal.start(qsQuestion.sID,
					qsQuestion.iMajor+"."+qsQuestion.iMinor,
					"frog",nvp.getNames(),nvp.getValues(),null);
				if(DEBUG)
					System.err.println(sr.getXHTML());
				sSession=sr.getQuestionSession();
			}
			long lStartTime=System.currentTimeMillis()-lBefore;
			lThings.add(lStartTime+"");
			tsStart.add(lStartTime);
			
			// Run each sequence
			for(int iSequence=1;;iSequence++)
			{
				// Delay
				if(r!=null) Thread.sleep(r.nextInt(DELAYMAX-DELAYMIN+1)+DELAYMIN);
				
				// Build up params
				nvp=new NameValuePairs();
				boolean bFound=false;
				for(int iStep=0;iStep<qsQuestion.asSteps.length;iStep++)
				{
					Step s=qsQuestion.asSteps[iStep];
					if(s.iSeq==iSequence)
					{
						nvp.add(s.sName,s.sValue);
						bFound=true;
					}
				}
				if(!bFound) break;
				
				// Send action
				boolean bQuestionEnd=false;
				lBefore=System.currentTimeMillis();
				if(osRemote!=null)
				{
					om.axis.qengine.ProcessReturn pr=
						osRemote.process(sSession,nvp.getNames(),nvp.getValues());
					if(DEBUG)
						System.err.println(pr.getXHTML());
					bQuestionEnd=pr.isQuestionEnd();
				}
				else
				{
					om.qengine.ProcessReturn pr=
						osLocal.process(sSession,nvp.getNames(),nvp.getValues());
					if(DEBUG)
						System.err.println(pr.getXHTML());
					bQuestionEnd=pr.isQuestionEnd();
				}
				long lProcess=System.currentTimeMillis()-lBefore;
				lThings.add(lProcess+"");
				tsProcess.add(lProcess);
				
				if(LEAVESESSIONS) break;
				if(bQuestionEnd) break;
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			lThings.add("ERROR");
		}
		
		System.out.println(Strings.join("\t",lThings));
	}
	
	
}
