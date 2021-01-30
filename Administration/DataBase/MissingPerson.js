var mongoose = require('mongoose');

option = { useNewUrlParser: true };
mongoose.connect("mongodb+srv://zweichen:G2ITBTCES@cluster0-75ahh.mongodb.net/test?retryWrites=true").then(
  () => { console.log('Successfully connected to MongoDB!'); },
  err => { console.log('Error when connecting to MongoDB! ' + err); }
);

var Schema = mongoose.Schema;

var missingPersonSchema = new Schema({
    name: {type: String, required: true, unique: true},
    lastSeenStreet: {type: String, required: true},
    lastSeenCity: {type: String, required: true},
    lastSeenState: {type: String, required: true},
    description: {type: String, required: true},
    contactNumber: {type: Number, required: true},
    isRequest: {type: Boolean, required: true},
    image: {type: String, required: false},
    imageBase64: {type: String, required: false},
    hasImage: {type: Boolean, required: true}
});

module.exports = mongoose.model('MissingPerson', missingPersonSchema);
