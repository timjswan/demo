/**
 * Add options to the editions dropdown from the list of editions 
 * in the rel of the selected channel dropdown
 * 
 * @param editions - the editions collected from the siteConfig
 * @param editionId - the unique id of the select tag instance
 */
function updateEditionDropdown(editions, editionId) {
	var select = $('#' + editionId);
	
	// Clear the select control
	select.empty();

	if (editions !== "") {
		var editionsArr = editions.split("|");
		for (var i = 0; i < editionsArr.length; i++) {
			var ed = editionsArr[i];
			select.append($("<option />").val(ed).text(ed));
		}
	}
}

/**
 * If the user has selected 'whole' as the region scope then we need to loop through all the
 * products in each region and select them
 * 
 * @param regionScopeCheck - the DOM object of the checkbox the user has checked
 */
function changeAllRegions(regionScopeCheck){
	var checked = regionScopeCheck.prop("checked");
	var checks = regionScopeCheck.parent()
					.find("ul li .regionCheck");

	checks.each(function(){
		$(this).prop("checked", checked);
	});
}

var count = 0;

/**
 * Make an ajax call to a jsp that gets the context attribute 
 * set in com.eidosmedia.tags.newUseCase.ProgressHelper
 * 
 * This is called every second until the create campaign
 * ajax call returns a success.
 */
function getProgress(){	
	randomNo = Math.floor((Math.random() * 100) + 1);
	buffer = "";
	$.ajax({
		type: "POST",
		url: "http://globevm:31143/webclient/WebClientCtx/custom/jsp/eidosmedia/campaign/progress.jsp?random=" + randomNo
	}).always(function(data){
		console.log(count + ", " + "DATA: " + data);
		var progressFull = data.trim(),
		progress = progressFull.split("-")[1].trim();

		$("#progress").html(progressFull + "%");
	});

	count++;
}

$(document).ready(function() {
	var app = window.external.EomQueryInterface('methode.application');
	var w = app.ActiveWindow;
	
	// Get the session key so we can create a new campaign via ajax call
	var skeyIp   = w.GetSiteConfigString("/siteConfiguration/webClient/@host");
	var skeyPort = w.GetSiteConfigString("/siteConfiguration/webClient/@port");
	var skey = app.ActiveWindow.GetServletSessionKey("http://" + skeyIp + ":" + skeyPort + "/webclient");
	
	// The title name is needed for maintainability
	var titleNameFull = w.GetSiteConfigString("/siteConfiguration/titleName");
	var titleName = titleNameFull.replace("/", "");
	
	// dsmHelper is a helper object that handles datasources
	dsmHelper.setErrorContainer("#error");
	
	// Load 'products' datasource
	dsmHelper.setContext("products");	
	dsmHelper.load();	
	dsmHelper.updateHTMLSelectById("#product");
	
	// Load 'scope' datasource
	dsmHelper.setContext("scope");
	dsmHelper.load();
	dsmHelper.updateHTMLSelectById("#scope");	
	
	// When the channel dropdown changes, get the editions and display them in the adjacent dropdown
	$(".channel").each(function() {
		$(this).change(function() {
			var optionSelected = $("option:selected", this);
			var optionRel = optionSelected.attr("rel");
			var editionSibling = $(this).siblings(".edition");
			var editionId = editionSibling.attr("id");
			updateEditionDropdown(optionRel, editionId);
		});
	});
	
	// When a campaign has been created and progress is updated clear the text when the 
	// user starts a new campaign
	$("#newCampaignForm input").change(function(){
		if($(".clearable").text().length > 0){
			$(".clearable").text("");
		}
	});
	
	// For each region checkbox tick all the children if whole region is selected
	$("#newCampaignForm .regionSelector").change(function(){		
		var regionScope = $("#regionScope").val();
		
		if(regionScope === "whole"){
			changeAllRegions($(this));
		}
	});
	
	// UI functionality that hides/shows all region checkbox children
	$("#newCampaignForm #regionTree > ul > li > a").each(function(){
		$(this).click(function(){
			$(this).parent().toggleClass("open closed");
		});
	});
	
	// Validate the version entered by the user. This should be alphanumerical
	$("#version").blur(function(){
		var pattern = /[|&;$%@"<>()+,?\/]/g;
		var currentValue = $(this).val();
		if(currentValue.match(pattern)){
			var cleanValue = currentValue.replace(pattern, "");
			currentValue = cleanValue;
		}
		$(this).val(currentValue);
	});

	// On form submit
	$("#newCampaignForm #submit").click(function(e){
		e.preventDefault();

		// Gather DOM objects for the region checkboxes
		var regionChecks = $("#newCampaignForm input[name='regionCheck']");
		var selectedRegions = [];
		var unselectedRegions = 0;
		
		// For each checked checkbox add it to the selectedRegions list
		// If one is not checked keep a count
		regionChecks.each(function(){
			if($(this).prop("checked")){
				selectedRegions.push(this.value);			
			} else {
				unselectedRegions++;
			}
		});
		
		// No regions selected
		if(unselectedRegions === regionChecks.length){
			$("#error").text("No region/customer selected");
			throw new Error("No region/customer selected");
		}		
		
		// Get the data to pass via AJAX to com.eidosmedia.tags.newUseCase.NewUseCaseTag
		var version = $("#version").val();
		var product = $("#product").val();
		var scope = $("#scope").val();
		var regions = selectedRegions.toString();
		
		window.interval = setInterval(getProgress, 1000);
		
		$.ajax({
			type: 'POST',
			url: "/webclient/WebClientCtx/custom/jsp/eidosmedia/campaign/useCase.jsp",
			data: {
				'skey': skey,
				'titlename': titleName, 
				'regions': regions,
				'version': version,
				'product': product,
				'scope': scope
			},
			success: function(){
				clearInterval(window.interval);
			}
		}).fail(function(data){
			$("#progress").html(data.trim());
		});
	});
});