
class HashFunction {

    // Fields
    // * Object md
    // * String maxHash

    constructor(name, hashSize) {
        if (name === "sha512") {
            this.md = forge.md.sha512.create();
        } else {
            this.md = forge.md.sha256.create();
        }
        this.maxHash = "f".repeat(hashSize / 4);
    }
}

class PowMining {

    // Fields:
    // String hashFunction ... "sha512" / "sha256"
    // String bestHash
    // Number iteration = 0
    // Number nonce = 0
    // Number timeLimit ... allowed time in seconds
    // md ... hashing processor
    // onNewBestFn ... callback called when a new best hash is found
    // onIterationDoneFn ... callback called when an iteration is done
    // doneFn ... when the mining process is completely done

    constructor(hashFunction, timeLimit, seed, onNewBestFn, onIterationDoneFn, doneFn) {
        console.log("constructor(hashFunction:%s, timeLimit:%d, seed:%s)", hashFunction, timeLimit, seed);

        this.processStartTime = null;
        this.hashFunction = hashFunction;
        this.iteration = 0;
        this.timeLimit = timeLimit;
        this.seed = seed;
        this.onNewBestFn = onNewBestFn;
        this.onIterationDoneFn = onIterationDoneFn;
        this.doneFn = doneFn;
        this.stop = false;
    }

    // public method
    start() {
        console.log("start()");

        this.processStartTime = new Date();
        this.bestHash = this.hashFunction.maxHash;
        this.nonce = 0;
        this.iteration = 0;
        setTimeout(this.computeFn.bind(this), 500);
    }

    stop() {
        console.log("stop()");
        this.stop = true;
    }

    // private method
    computeFn() {
        if (this.stop) {
            return;
        }

        this.iteration++;

        //console.log("computeFn(): %s :: Start iteration:%d; nonce:%d", new Date().toUTCString(), this.iteration, this.nonce);

        let blink = false;
        let startTime = new Date();

        while (!this.stop) {
            this.nonce++;
            this.hashFunction.md.update(this.nonce.toString() + this.seed);
            let newHash = this.hashFunction.md.digest().toHex();

            if (newHash < this.bestHash) {
                this.bestHash = newHash;
                console.log("New best hash:%s", this.bestHash);
                blink = true;
            }

            if (this.nonce % 1000 === 0) {
                if (new Date() - startTime >= 200) {
                    //console.log("computeFn(): %s :: Break iteration:%d; nonce:%d", new Date().toUTCString(), this.iteration, this.nonce);
                    break;
                }
            }
        }

        if (blink) {
            this.onNewBestFn(this.bestHash);
        }

        let seconds = Math.round(this.timeLimit - (new Date() - this.processStartTime) / 1000);

        if (seconds <= 0) {
            this.doneFn();
        } else {
            this.onIterationDoneFn(seconds, this.nonce);
            setTimeout(this.computeFn.bind(this), 5);
        }
    }
}