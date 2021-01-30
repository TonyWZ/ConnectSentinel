var mongoose = require('mongoose');

option = { useNewUrlParser: true };
mongoose.connect("mongodb+srv://zweichen:G2ITBTCES@cluster0-75ahh.mongodb.net/test?retryWrites=true", option).then(
    () => { console.log('Successfully connected to MongoDB!'); },
    err => { console.log('Error when connecting to MongoDB! ' + err); }
);

var Schema = mongoose.Schema;

var banUserSchema = new Schema({
    id: {type: Number, required: true},
    password: {type: String, required: true}
});

module.exports = mongoose.model('BanUser', banUserSchema);
