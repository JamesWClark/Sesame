/**

JW Clark

load XML from file upload, parse it

started at  http://www.html5rocks.com/en/tutorials/file/dndfiles/
learned a closure - http://stackoverflow.com/questions/12546775/get-filename-after-filereader-asynchronously-loaded-a-file

*/

// Check for the various File API support.
if (window.File && window.FileReader && window.FileList && window.Blob) {
	console.log('All the File APIs are supported.');
} else {
	alert('The File APIs are not fully supported in this browser.');
}

var totalDocumentsProcessed = 0;
var totalDocumentCount = 0;
var totalWordCount = 0;
var totalLemmaCount = 0;
var collectDocuments = {};
var collectLemmas = {};
var collectWords = {};
var wordNotCounted = [];

function reset() {
	totalDocumentsProcessed = 0;
	totalDocumentCount = 0;
	totalWordCount = 0;
	totalLemmaCount = 0;
	collectDocuments = {};
	collectLemmas = {};
	collectWords = {};
	wordNotCounted = [];
	$('output').html('');
}

function tf() {

}

function idf() {

}

function SentimentFormatException(sentiment) {
	this.value = sentiment;
	this.message = "Unhandled sentiment expression";
	this.toString = function () {
		return this.value + " : " + this.message;
	};
}

function evaluateSentimentLabelScore(sentimentLabel) {
	switch (sentimentLabel) {
		case "Verypositive":
			return 2;
		case "Positive":
			return 1;
		case "Neutral":
			return 0;
		case "Negative":
			return -1;
		case "Verynegative":
			return -2;
		default:
			throw new SentimentFormatException(sentimentLabel);
			break;
	}
}

function incrementLemmaCounts(lemma) {
	if (!(lemma in collectLemmas))
		collectLemmas[lemma] = 1;
	else
		collectLemmas[lemma]++;
	totalLemmaCount++;
}

function incrementWordCounts(word) {
	if (word.match(/^[0-9a-z]+$/) || word.length > 1 || word === "i".toUpperCase()) {
		if (!(word in collectWords))
			collectWords[word] = 1;
		else
			collectWords[word]++;
		totalWordCount++;
	}
	else
		wordNotCounted.push(word);
}

function parseCoreNLPXML() {

	//foreach document
	for (var fileName in collectDocuments) {
		if (collectDocuments.hasOwnProperty(fileName)) {

			var parser = new DOMParser();
			var xml = parser.parseFromString(collectDocuments[fileName], "application/xml");
			var sentences = xml.getElementsByTagName('sentences')[0].children;
			var totalSentimentScore = 0;
			var html = '';

			//foreach sentence
			for (var i = 0; i < sentences.length; i++) {
				var sentimentScore = parseInt(sentences[i].attributes[1].nodeValue);
				var sentimentLabel = sentences[i].attributes[2].nodeValue;
				var tokens = sentences[i].children[0].children;

				//foreach token
				var tokenSentence = '';
				for (var k = 0; k < tokens.length; k++) {
					var iWord = 0;
					var iLemma = 1;
					var iPos = 4;
					var iNer = 5;
					var iSpeaker = 6;
					var word = tokens[k].children[iWord].textContent;
					var lemma = tokens[k].children[iLemma].textContent;
					incrementWordCounts(word);
					incrementLemmaCounts(lemma);
					tokenSentence += word + ' ';
				}
				totalSentimentScore += evaluateSentimentLabelScore(sentimentLabel);

				html += '<br><div>' + (i + 1) + ': ' + sentimentLabel + '</div><div>' + tokenSentence + '</div>';
			}
			var containerDocument = '<div id="' + fileName + '">' + html + '</div>';
			$('#totalSentimentScore').html('<b>Total Sentiment Score:</b> ' + totalSentimentScore);
			$('#totalWordCount').html('<b>Word Count:</b> ' + totalWordCount);
			$('#totalLemmaCount').html('<b>Lemma Count:</b> ' + totalLemmaCount);
			$('#totalSentences').html('<b>Sentences:</b> ' + sentences.length);
			$('#container-documents').append(containerDocument);

			//console.log(sentences);
			//console.log(wordNotCounted);
		}
	}
	var lemmaList = $('#lemma-frequency');
	for (var key in collectLemmas) {
		if (collectLemmas.hasOwnProperty(key)) {
			$(lemmaList).append('<li>' + key + ': ' + (collectLemmas[key] / totalLemmaCount).toFixed(5) + '</li>')
		}
	}

	var wordList = $('#word-frequency');
	for (var key in collectWords) {
		if (collectWords.hasOwnProperty(key)) {
			$(wordList).append('<li>' + key + ': ' + (collectWords[key] / totalWordCount).toFixed(5) + '</li>')
		}
	}
	$('#display').show();
}


function handleFileSelect(event) {
	reset();
	var files = event.target.files; // FileList object
	totalDocumentCount = files.length;
	for (var i = 0, f; f = files[i]; i++) {
		var fileReader = new FileReader();
		fileReader.onloadend = (function (file) {
			return function (evt) {
				collectDocument(evt, file)
			}
		})(f);
		fileReader.readAsText(f);
	}
}

function collectDocument(evt, file) {
	collectDocuments[file.name] = evt.target.result;
	if (++totalDocumentsProcessed === totalDocumentCount)
		parseCoreNLPXML();
}

document.getElementById('files').addEventListener('change', handleFileSelect, false);