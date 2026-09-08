package com.sanket_satpute_20.ironmind.failure

class InMemoryFailureStorage : FailureStorage {
    override var latestJson: String = ""
    override var historyJson: String = ""
    override var recoveryJson: String = ""
}
