// Configure the setup editor
var editorConfig = ace.edit("editorConfig", { wrap: true });
editorConfig.setTheme("ace/theme/terminal");
editorConfig.setShowPrintMargin(false);
editorConfig.session.on('change', () => { 
	editorConfig.renderer.scrollToLine(Number.POSITIVE_INFINITY);
});

// Configure the read only console
var editorConsole = ace.edit("editorConsole", { wrap: true });
editorConsole.setTheme("ace/theme/terminal");
editorConsole.setShowPrintMargin(false);
editorConsole.setReadOnly(true);
editorConsole.session.on('change', () => { 
	editorConsole.renderer.scrollToLine(Number.POSITIVE_INFINITY);
});

// Update console every second
setInterval(function() {
	$.get("/console", function( result ) {
		if (editorConsole.getValue().trim() != result.trim()) {
			editorConsole.setValue(result.trim(), 1);
		}
	});
}, 1000);

// Make call to server when an execution is requested
$("#requestExecution").click(function(e){
	console.log("Execution requested");
	 $.ajax({
		  url:'requestExecution',
		  type: "POST",
		  data: JSON.stringify({ 
			  "setup" : editorConfig.getValue(),
		  }),
		  contentType:"application/json; charset=utf-8",
		  dataType:"json",
		  function(data){
			  console.log("Execution request sent");
		  }
	});
});

$("#allowExecution").click(function(e){
	let cipher = $("#cipher").val();
	console.log("Execution allowed with cipher " + cipher);
	 $.ajax({
		  url:'allowExecution',
		  type: "POST",
		  data: JSON.stringify({ 
			  "key" : cipher,
		  }),
		  contentType:"application/json; charset=utf-8",
		  dataType:"json",
		  function(data){
			  console.log("Execution request sent");
		  }
	});
});


function startJob(machineIds, setup) {
	 
	}