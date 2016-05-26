#!/bin/bash


/local/kurs/mt/mosesdecoder/scripts/training/train-model.perl --corpus /local/kurs/mt/projects/data/de-en.train --f de --e en --root-dir moses.output --lm 0:5:/home/stp15/caroa/Documents/mtproj/de-en.train.lm.en --external-bin-dir /local/kurs/mt/bin -distortion-limit 0  >logfile 2>&1


/local/kurs/mt/mosesdecoder/bin/moses -f moses.output/model/moses.ini  -i /local/kurs/mt/projects/data/de-en.dev.de > de-en.dev.out.de

/local/kurs/mt/mosesdecoder/scripts/generic/multi-bleu.perl /local/kurs/mt/projects/data/de-en.dev.en < de-en.dev.out.de > bleu-mono.txt