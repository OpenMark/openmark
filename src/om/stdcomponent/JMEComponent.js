function jmeSet(id,idprefix,smiles)
{
  var input=document.getElementById(idprefix+"omval_"+id);
  input.value=smiles;
  var action=document.getElementById(idprefix+"omact_"+id);
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

function jmeClick(resourcesPath,id,idprefix)
{
  if(jmePopup!=null)
  {
    jmePopup.focus();
    return;
  }
  jmePopup=window.open("",idprefix.replace(/\W/,"_")+"jme"+id,"width="+
    (isIE ? "284" : "288") +
    ",height="+
    (isIE ? "347" : "350")+
    ",menubar=no,resizable=yes,scrollbars=no,dependent=yes");

  jmePopup.document.write(
    "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>\n"+
    "<head><title>Java Molecular Editor</title></head>\n"+
    "<body style='border:0;margin:0;' onunload='try { window.opener.jmePopup=null;} catch(e){}'>\n"+
    "<applet code='JME.class' name='JME' archive='"+resourcesPath+"/jme.jar' width='288' height='312'>\n"+
    "The JME requires Java to be enabled and installed. Please enable Java, then close "+
    "this window and try again.\n"+
    "</applet>\n"+
    "<div style='position:relative;'>\n"+
	"<div style='color:#999; font: 10px Verdana, sans-serif; padding: 6px 0 0 4px; position:absolute; width:150px; left:0px;'><a href='http://www.molinspiration.com/jme/index.html' target='_blank' style='color:#999'>JME Editor</a> courtesy of Peter Ertl, Novartis</div>\n"+
    "<div style='text-align:right;padding-right:8px;padding-top:8px;'>\n"+
    "<input type='button' id='enter' value='    OK    ' onclick='window.opener.jmeSet(\""+id+"\",\""+idprefix+"\",document.JME.smiles());'/>\n"+
    "</div></div>\n"+
    "<script type='text/javascript'>\n"+
    "var exceptionCounter = 0;\n"+
    "function check()\n"+
    "{\n"+
      // Check gone to other page (=>not this question)
      "try\n"+
      "{\n"+
	    "if(typeof(window.opener)=='undefined' || window.opener.location.protocol!='"+location.protocol+"' ||\n"+
            "window.opener.location.host!='"+location.host+"' ||window.opener.location.pathname!='"+location.pathname+"') {\n"+
          "window.close();\n"+
          "return;\n"+
        "}\n"+
        // Check if mid-loading, in which case don't close it (yet)
        "if(typeof(window.opener.mLoaded)=='undefined' && window.opener.document.getElementById('footer') == null) { return; }\n"+
        // Check if page has JME and matches this one, if not then bail
        "if(!window.opener.mJME || window.opener.mJME!='"+window.mJME+"') { window.close(); return; }\n"+
        // OK, update popup storage and the enabled-state
        "window.opener.jmePopup=window;\n"+
        "document.getElementById('enter').disabled=window.opener.document.getElementById('"+idprefix+id+"_button').disabled;\n"+
        "exceptionCounter = 0;\n"+
      "}\n"+
      // We will get some sort of security exception if the user has gone to another domain, however,
      // sometimes IE throws the same error in the middle of a page load, when you are staying
      // on the same site, so only close if you get it three times.
      "catch(e)\n"+
      "{\n"+
        "exceptionCounter = exceptionCounter + 1;\n"+
        "if (exceptionCounter < 3) { return; }\n"+
        "window.close(); return;\n"+
      "}\n"+
    "}\n"+
    "window.setInterval('check()',250);\n"+
    "</script>\n"+
    "</body></html>\n");
  jmePopup.document.close();
}