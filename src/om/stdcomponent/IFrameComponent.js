function sendResponse(id,idprefix,iff)
{
//alert(""+id+" "+iff);
	var answer="", docw=document.getElementById(iff).contentWindow, docc;
	if(docw==null)docc=document.getElementById(iff).contentDocument;
	else docc=docw.document;
	answer=docc.getElementById('response').innerHTML;
	id=id.replace("omval_","");
	var input=document.getElementById(idprefix+"omval_"+id);
	input.value=answer;
	var action=document.getElementById(idprefix+"omact_"+id);
	if(action)
	{
		action.disabled=false;
		preSubmit(input.form);
		input.form.submit();
	}
}
