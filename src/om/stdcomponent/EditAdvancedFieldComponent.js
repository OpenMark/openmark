var mysubtype=mysubtype||new Array();

function editadvancedfieldFix(fieldName,idPrefix,enabled,type,dZoom, sfg,sbg) {
	if(enabled) {
		setTimeout(function() { editadvancedfieldInit(fieldName,idPrefix, type) },0);
		var configScript = document.getElementById(fieldName + "_config");
		configScript.setAttribute('src', configScript.getAttribute('data-src'));
	}
}

function editadvancedfieldInit(fieldName, idPrefix, type) {	
	addPreSubmit(document.getElementById(idPrefix+"omval_"+fieldName).form,
		function() { editadvancedfieldPreSubmit(fieldName, idPrefix, type); });
	editadvancedfieldSupSubInit(fieldName, idPrefix, type);
}

function editadvancedfieldPreSubmit(fieldName, idPrefix, type) {
    var id = idPrefix+"om_" + fieldName + "_iframe";
    var editor = tinyMCE.get(id);
    var content = editor.getContent();
    document.getElementById(idPrefix+"omval_"+fieldName).value = content;
}

/**
 * Disable Subscript option in tinymce supsub plugin
 * @param string fieldName name of target field
 * @param string idPrefix 
 * @param string type of editor available
 */
function editadvancedfieldSupSubInit(fieldName, idPrefix, type){
    var editor = tinyMCE.get(idPrefix+"om_" + fieldName + "_iframe");
    if(!editor){
        setTimeout(function() { editadvancedfieldSupSubInit(fieldName, idPrefix, type),50});
        return;
    }
    switch(type){
        case 'subscript':
            // Disable superscript
            editor.execCommand('supsubSetEnabled', 'superscript', false);
            break;
        case 'superscript':
            // Disable subscript
            editor.execCommand('supsubSetEnabled', 'subscript', false);
            break;
    }

}
