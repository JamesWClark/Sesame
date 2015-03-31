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


function TFIDF() {

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

function parseCoreNLPXML() {
	var parser = new DOMParser();
	var xml = parser.parseFromString(fileReader.result, "application/xml");

	var sentences = xml.getElementsByTagName('sentences')[0].children;
	console.log(sentences);

	var totalSentimentScore = 0;

	//foreach sentence
	for (var i = 0; i < sentences.length; i++) {
		var sentimentScore = parseInt(sentences[i].attributes[1].nodeValue);
		var sentimentLabel = sentences[i].attributes[2].nodeValue;
		var tokens = sentences[i].children[0].children;

		totalSentimentScore += evaluateSentimentLabelScore(sentimentLabel);
		//console.log(sentences[i].children[0].children[0].children[0].textContent);


		//foreach token
		var tokenSentence = '';
		for (var k = 0; k < tokens.length; k++) {
			var iWord = 0;
			var iLemma = 1;
			var iPos = 4;
			var iNer = 5;
			var iSpeaker = 6;
			tokenSentence += tokens[k].children[iWord].textContent + ' ';
		}

		$('#list').append('<br><div>' + (i + 1) + ': ' + sentimentLabel + '</div><div>' + tokenSentence + '</div>');

	}
	$('#summarySentimentValue').html('<b>Total Sentiment Score:</b> ' + totalSentimentScore);

	//var attr = sentence.attributes[2];
	//document.write(attr.value);

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