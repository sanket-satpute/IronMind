package com.sanket_satpute_20.ironmind.failure

interface FailureStorage {
    var latestJson: String
    var historyJson: String
    var recoveryJson: String
}
