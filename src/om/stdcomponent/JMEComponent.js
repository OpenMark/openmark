function jmeSet(id,smiles)
{
  var input=document.getElementById("omval_"+id);
  input.value=smiles;
  var action=document.getElementById("omact_"+id);
  if(action)
  {
    action.disabled=false;
    preSubmit(input.form);
    input.form.submit();
  }
}

function jmeInit(token)
{
  window.mJME=token;
}

var jmePopup=null;

function jmeClick(resourcesPath,id)
{
  if(jmePopup!=null)
  {
    jmePopup.focus();
    return;
  }
  jmePopup=window.open("","jme"+id,"width="+
    (isIE ? "284" : "288") +
    ",height="+
    (isIE ? "347" : "350")+
    ",menubar=no,resizable=yes,scrollbars=no,dependent=yes");
  
  jmePopup.document.write(
    "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>"+
    "<head><title>Java Molecular Editor</title></head>"+
    "<body style='border:0;margin:0;' onunload='try { window.opener.jmePopup=null;} catch(e){}'>"+
    "<applet code='JME.class' name='JME' archive='"+resourcesPath+"/jme.jar' width='288' height='312'>"+
    "The JME requires Java to be enabled and installed. Please enable Java, then close "+
    "this window and try again."+
    "</applet>"+
    "<div style='position:relative;'>"+
	"<div style='color:#999; font: 10px Verdana, sans-serif; padding: 6px 0 0 4px; position:absolute; width:150px; left:0px;'><a href='http://www.molinspiration.com/jme/index.html' target='_blank' style='color:#999'>JME Editor</a> courtesy of Peter Ertl, Novartis</div>"+    
    "<div style='text-align:right;padding-right:8px;padding-top:8px;'>"+
    "<input type='button' id='enter' value='    OK    ' onclick='window.opener.jmeSet(\""+id+"\",document.JME.smiles());'/>"+
    "</div></div>"+
    "<script type='text/javascript'>"+
    "function check() "+
    "{ "+
      // Check gone to other page (=>not this question)
      "try "+
      "{"+
	      "if((!window.opener) || window.opener.location.href!='"+location.href+"') { window.close(); return; }"+
	    "}"+
	    "catch(e) { window.close(); return; }"+
      // Check if mid-loading, in which case don't close it (yet)
      "if(window.opener.mLoaded==undefined) return; "+ 
      // Check if page has JME and matches this one, if not then bail
      "if(!window.opener.mJME || window.opener.mJME!='"+window.mJME+"') { window.close(); return; } "+
      // OK, update popup storage and the enabled-state
      "window.opener.jmePopup=window; "+
      "document.getElementById('enter').disabled=window.opener.document.getElementById('"+id+"_button').disabled; "+
    "}"+
    "window.setInterval('check()',250);"+
    "</script>"+
    "</body></html>");
  jmePopup.document.close();
}