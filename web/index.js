function App() {
}

App.prototype = {
    "setUp": function() {
        console.log("setUp");
        
        var feedback = document.getElementById("feedback-link");
        feedback.addEventListener("click", this.sendFeedback.bind(this));
    },

    "start": function() {
    },

    "sendFeedback": function() {
        console.log("sendFeedback");
        window.location.href = 'mailto:app.eloquent@gmail.com?subject=Feedback&body=Dear Eloquent authors, here\'s what I like and what I don\'t like about the app, the website or the idea in general.';
        
    },
};

var app = new App();
app.setUp();
