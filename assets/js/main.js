/**

JW Clark
started at  http://www.html5rocks.com/en/tutorials/file/dndfiles/
load XML from file upload, parse it

*/

// Check for the various File API support.
if (window.File && window.FileReader && window.FileList && window.Blob) {
	console.log('All the File APIs are supported.');
} else {
	alert('The File APIs are not fully supported in this browser.');
}


var fileReader;
var totalWordCount = 0;
var totalLemmaCount = 0;
var wordNotCounted = [];
var collectLemmas = {};
var collectWords = {};

/**
* Throws 
*/
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
	var parser = new DOMParser();
	var xml = parser.parseFromString(fileReader.result, "application/xml");
	var sentences = xml.getElementsByTagName('sentences')[0].children;
	var totalSentimentScore = 0;

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

		$('#list').append('<br><div>' + (i + 1) + ': ' + sentimentLabel + '</div><div>' + tokenSentence + '</div>');
	}
	$('#totalSentimentScore').html('<b>Total Sentiment Score:</b> ' + totalSentimentScore);
	$('#totalWordCount').html('<b>Word Count:</b> ' + totalWordCount);
	$('#totalLemmaCount').html('<b>Lemma Count:</b> ' + totalLemmaCount);
	$('#totalSentences').html('<b>Sentences:</b> ' + sentences.length);
	
	var lemmaList = $('#lemma-frequency');

	Object.keys(collectLemmas).sort(function (a, b) { return collectLemmas[a] - collectLemmas[b] });

	for (var key in collectLemmas) {
		if (collectLemmas.hasOwnProperty(key)) {
			$(lemmaList).append('<li>' + key + ': ' + (collectLemmas[key]/totalLemmaCount).toFixed(5) + '</li>')
		}
	}

	var wordList = $('#word-frequency');

	Object.keys(collectWords).sort(function (a, b) { return collectWords[a] - collectWords[b] });

	for (var key in collectWords) {
		if (collectWords.hasOwnProperty(key)) {
			$(wordList).append('<li>' + key + ': ' + (collectWords[key]/totalWordCount).toFixed(5) + '</li>')
		}
	}
	console.log(sentences);
	console.log(wordNotCounted);
}

function handleFileSelect(evt) {
	var files = evt.target.files; // FileList object
	for (var i = 0, f; f = files[i]; i++) {
		fileReader = new FileReader();
		fileReader.readAsText(f);
		fileReader.onloadend = parseCoreNLPXML;
	}
}

document.getElementById('files').addEventListener('change', handleFileSelect, false);