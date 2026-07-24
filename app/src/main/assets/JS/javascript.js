//JavaScript alerts don't work on android
function showToast() {
    alert("here we go2");
    JSInterface.showToast();
}

function goToTitleScreen() {
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
    // Skip font adjustment for the TOC grid to preserve specific branding fonts
    if (document.body.classList.contains('toc-page') || document.body.classList.contains('toc-page-body')) return;

    var styleId = 'dynamic-font-style';
    var styleElement = document.getElementById(styleId);
    if (!styleElement) {
        styleElement = document.createElement('style');
        styleElement.id = styleId;
        document.head.appendChild(styleElement);
    }
    var baseSize = parseInt(fontInput);
    styleElement.innerHTML =
        'body, p, li, center, div, span, font, #mainContent, #headerContent, #bibleText, .text-container, .T14, .t14, .T4, .T1, .subtitle { font-size: ' + baseSize + 'pt !important; line-height: 1.4 !important; font-family: Georgia, Serif !important; } ' +
        'h1, h2, h3, .title { font-size: ' + baseSize + 'pt !important; font-weight: bold !important; } ' +
        '#verseNumber { font-size: ' + baseSize + 'pt !important; } ' +
        '#bibleReference, .italic, #italicText, .T3 { font-size: ' + (baseSize - 2) + 'pt !important; }' +
        '.whyPage, .keysPage { font-size: ' + (baseSize - 1) + 'pt !important; } ' +
        '.fa { font-family: FontAwesome !important; }';
}

function searchForText(text1,text2) {
    document.getElementById('mainContent').innerHTML = "";
    JSInterface.searchForText(text1,text2);
}

function insertHeader(input) {
    var header = document.getElementById("headerContent");
    if (header) header.innerHTML = input;
}

function insertTable(input) {
    var table = document.getElementById("chapterSelectTable");
    if (table) table.innerHTML = input;
}

function insertBody(input) {
    var textElement = document.createElement("span");
    textElement.innerHTML = input.toString().replace(new RegExp("&quot;", 'g'), "'");
    var mainContent = document.getElementById("mainContent");
    if (mainContent) mainContent.appendChild(textElement);
}

function insertFunction(text) {
    insertBody(text);
}

function insertPreviousChapterAnchor() {
    var mainContent = document.getElementById("mainContent");
    if (!mainContent) return;
    var anchor = document.getElementById("previousChapterAnchor");
    if (!anchor) {
        anchor = document.createElement("div");
        anchor.id = "previousChapterAnchor";
        mainContent.prepend(anchor);
    }
}

function insertBeforeFunction(text) {
    var mainContent = document.getElementById("mainContent");
    if (!mainContent) return;
    var textElement = document.createElement("div");
    textElement.id = "bibleText";
    textElement.innerHTML = text.toString().replace(new RegExp("&quot;", 'g'), "'");
    mainContent.prepend(textElement);
}

function scrollToAnchor() {
    // Basic implementation
    window.scrollTo(0,0);
}

  $(function () {
            $("#footer").load("file:///android_asset/pageElements/footer.html");
  });

