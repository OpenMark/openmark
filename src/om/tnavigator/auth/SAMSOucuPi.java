package om.tnavigator.auth;

import java.net.URL;

import javax.xml.namespace.QName;

import om.Log;
import om.getOucuInfo.GetOucuInfoRequest;
import om.getOucuInfo.GetOucuInfoSoap;
import om.getOucuInfo.GetOucuInfo_Service;
import om.getOucuInfo.OucuInfo;
import om.getOucuInfo.RequestHeader;
import om.getOucuInfo.User;
import om.getOucuInfo.UserIds;
import om.tnavigator.NavigatorConfig;


public class SAMSOucuPi {
	
	private String oucu;
	private String pi;

	protected URL uSAMSValidWebService;
	protected String sSAMSServiceName;
	public static final String REQHEADERID="OpenMarkBatchProgram";
	public static final String REQHEADERUSER="slw2";
	public static final String REQHEADERSRC="OpenMark";
	
	public SAMSOucuPi(String oucu,String pi)
	{
		this.oucu=oucu;
		this.pi=pi;
		
	}

	
	  public SAMSOucuPi(String oucu,String dpi,NavigatorConfig nc,Log l)
	 {
		this.pi=dpi;
		this.oucu=oucu;
		 try
		 {
			 if (nc != null)
			 {
				URL uGetOucuInfoService = nc.getAuthParamURL("samsgetoucuinfoservice", true);
				String sSAMSOucuServiceName=nc.getAuthParamString("samsgetoucuinfoservicename", true);
				GetOucuInfo_Service service = new GetOucuInfo_Service(uGetOucuInfoService,new QName(sSAMSOucuServiceName, "GetOucuInfo"));	
				GetOucuInfoSoap what = service.getGetOucuInfoSoap();
				/* set up the header */
				RequestHeader reqHead=new RequestHeader();
				reqHead.setId(REQHEADERID);
				reqHead.setSource(REQHEADERSRC);
				User rUser= new User();
				rUser.setUserId(REQHEADERUSER);
				reqHead.setUser(rUser);
				
				/* now generate the request */
				
				GetOucuInfoRequest request=new GetOucuInfoRequest();
				request.setOucu(oucu);
				/* finally get the data */
				OucuInfo samsOucuPiData=what.getOucuInfo(request);
				UserIds ui=samsOucuPiData.getIds();

				/* so now we get the id. Look in the order, staff, tutor, student, */
				this.pi="";
				if (ui != null)
				{				
					this.oucu=ui.getOucu();
					if (this.pi.isEmpty() && !ui.getStaffId().isEmpty())
					{
						this.pi=ui.getStaffId();
					}
					if (this.pi.isEmpty() && !ui.getTutorId().isEmpty())
					{
						this.pi=ui.getTutorId();
					}
					if (this.pi.isEmpty() && !ui.getStudentId().isEmpty())
					{
						this.pi=ui.getStudentId();
					}
					if (this.pi.isEmpty() && !ui.getSelfRegisteredUserId().isEmpty())
					{
						this.pi=ui.getSelfRegisteredUserId();
					}
					if (this.pi.isEmpty() && !ui.getVisitorId().isEmpty())
					{
						this.pi=ui.getVisitorId();
					}
				}
				/* so if we didnt find anything then return what we cam in with */
				if (this.pi.isEmpty())
				{
					this.pi=dpi;
				}	
			 }
		 }
		 catch (Exception e)
		 {		
			 l.logDebug("Error code from oucupi sams call is " + e.getMessage() );
			 this.pi=dpi;
			 this.oucu=oucu;
		 }

	 }
		
		public void setOucu(String s)
		{
			oucu=s;
		}
		
		public void setPI(String p)
		{
			pi=p;
		}
		
		public String getOucu()
		{
			return oucu;
			
		}
		
		public String getPi()
		{
			return pi;
		}
		
		public boolean bOucuPiEqual()
		{
			return oucu.equals(pi);
		}
		
		public boolean piEmpty()
		{
			return pi.isEmpty();
		}
		
		public boolean oucuEmpty()
		{
			return oucu.isEmpty();
		}
		
		
}
