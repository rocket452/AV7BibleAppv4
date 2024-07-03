//JavaScript alerts don't work on android
function showToast() {
    alert("here we go2");
    //document.getElementById("logoTableAV7Cell").innerHTML = "Hello World";
    JSInterface.showToast();
}

function goToTitleScreen() {
    //document.getElementById("logoTableAV7Cell").innerHTML = "Hello Worldz";
    JSInterface.goToTitleScreen();
}

function goToTableOfContents() {
    JSInterface.goToTableOfContents();
}

function generateChapterPage() {

    JSInterface.generateChapterPage();
}

function showSelectChapterMenu() {
    JSInterface.showSelectChapterMenu();
}

function goToChapter(bookName, chapterNumber) {
    JSInterface.goToChapter(bookName, chapterNumber);
}

function loadNextChapter(bookName, chapterNumber) {
    JSInterface.loadNextChapter(bookName, chapterNumber);
}

function loadPreviousChapter(bookName, chapterNumber) {
    JSInterface.loadPreviousChapter(bookName, chapterNumber);
}



function goBack() {
    JSInterface.goBack();
}

function openOptionsMenu() {
    JSInterface.openOptionsMenu();
}

function openHelpPage() {
    JSInterface.openHelpPage();
}

function sendShareEmail() {
    JSInterface.sendShareEmail();
}

function clearCachedExtras() {
    JSInterface.clearCachedExtras();
}

function adjustFont(fontInput) {

    document.body.setAttribute('style', 'font-size:' + fontInput + 'pt !important;');
    document.getElementsByTagName("h1").setAttribute('style', 'font-size:' + fontInput + 'pt !important;');

}


function searchForText(text1,text2) {
	
    document.getElementById('mainContent').innerHTML = "";
    JSInterface.searchForText(text1,text2);
}


function insertHeaderAndFooter() {
	
    $("#headerInclude").append(
        "<table id='logoTable' style='width:100%;'>" +
        "<tr>" +
        "<td style='width:33%;'><img  style='width:100%;' id='av7IconImg'   src='images/Av7BarIcon.png' onclick='goToTitleScreen()'/></td>" +
        "<td style='width:50%;'><img id='invitationImg' style='width:100%;'  src='images/tib-logo.jpg' onclick='goToTableOfContents()'/></td>" +
        "</tr>" +
        "</table>");
		
    $("#footerInclude").append("<table id='footerTable'>" +
        "<tr >" +
        "<td onclick='goBack()'><i style='margin-left: 15%;' class='fa fa-arrow-left'></i></td>" +
        "<td  onclick='goBack()'>Back</td>" +
        "<td style='background-color:#439943; border: 1px solid gainsboro;'><a style='color: inherit; text-decoration: inherit;' href='NT/GoDeeperPage.htm'>Search</a></td>" +
        "<td style='background-color:#000287; border: 1px solid gainsboro;'><a style='color: inherit; text-decoration: inherit;' href='NT/sharePage.htm'>Share This</a></td>" +
        "<td onclick='openHelpPage()'>Help</td>" +
        "<td onclick='openOptionsMenu()'><i class='fa fa-cog'/></td>" +
        "</tr>" +
        "</table>");
}

function insertHeaderAndFooterNoOptions() {
	
    $("#headerInclude").append(
        "<table id='logoTable' style='width:100%;'>" +
        "<tr>" +
        "<td style='width:33%;'><img  style='width:100%;' id='av7IconImg'   src='images/Av7BarIcon.png' onclick='goToTitleScreen()'/></td>" +
        "<td style='width:50%;'><img id='invitationImg' style='width:100%;'  src='images/tib-logo.jpg' onclick='goToTableOfContents()'/></td>" +
        "</tr>" +
        "</table>");

    $("#footerInclude").append("<table id='footerTable'>" +
        "<tr >" +
        "<td><a style='color: inherit; text-decoration: inherit;' href='av7toc.htm'><i style='margin-left: 15%;' class='fa fa-arrow-left'></i></a></td>" +
        "<td><a style='color: inherit; text-decoration: inherit;' href='av7toc.htm'>Back</a></td>" +
        "<td style='background-color:#996E43; border: 1px solid gainsboro;'><a style='color: inherit; text-decoration: inherit;' href='GoDeeperPage.htm'>Go Deeper</a></td>" +
        "<td style='background-color:#000287; border: 1px solid gainsboro;' onclick='sendShareEmail()'>Share This</td>" +
        "<td onclick='openHelpPage()'>Help</td>" +
         "</tr>" +
        "</table>");
}

function insertBody(input) {

    var textElement = document.createElement("span");
	
	//input = input.toString().replace("&quot;","'");
	input = input.toString().replace(new RegExp("&quot;", 'g'), "'");
    textElement.innerHTML = input;

    var mainContent = document.getElementById("mainContent");
    //var mainContentTextNode = document.createTextNode(input);
    //mainContent.innerHTML = input;
    mainContent.appendChild(textElement);

}

function insertBefore(input) {
    var textElement = document.createElement("span");
    input = input.toString().replace(new RegExp("&quot;", 'g'), "'");
    textElement.innerHTML = input;

    var mainContent = document.getElementById("mainContent");
    // Insert at the top using prepend
    mainContent.prepend(textElement);
}