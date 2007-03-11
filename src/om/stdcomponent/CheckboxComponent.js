function checkboxOnClick(checkboxID)
{
  var checkbox=document.getElementById("omval_"+checkboxID);
  if (!checkbox.disabled) checkbox.checked = !checkbox.checked;
}

function checkboxFix(checkboxID)
{
  var container=document.getElementById(checkboxID);
  
  // Find ancestor row
  var row=container;
  while(row!=null && (row.tagName.toLowerCase()!='div' || row.className!='layoutgridrow'))
  {
    row=row.parentNode;
  }
  if(row==null) return; // Not in a layoutgrid

  // Fix height to same, less 10px for padding and border, less 4px padding on row
  // Note that I tried to use row.style.height but it doesn't work.
  container.style.height=(row.offsetHeight-14)+"px";
}