#!/bin/bash


/local/kurs/mt/srilm/bin/i686-m64/ngram-count -wbdiscount -text /home/stp15/caroa/Documents/mtproj/de-en.train.de -lm de-en.train.lm.de -order 10

/local/kurs/mt/srilm/bin/i686-m64/ngram-count -wbdiscount -text /local/kurs/mt/projects/data/de-en.train.en -lm de-en.train.lm.en -order 10

/local/kurs/mt/mosesdecoder/scripts/training/train-model.perl --corpus /home/stp15/caroa/Documents/mtproj.de-en.train --f de --e en --root-dir moses.output --lm 0:5:/home/stp15/caroa/Documents/mtproj/de-en.train.lm.en --external-bin-dir /local/kurs/mt/bin >logfile 2>&1


/local/kurs/mt/mosesdecoder/bin/moses -f moses.output/model/moses.ini -i /home/stp15/caroa/Documents/mtprojde-en.dev.de > de-en.dev.out.de

/local/kurs/mt/mosesdecoder/scripts/generic/multi-bleu.perl /local/kurs/mt/projects/data/de-en.dev.en < de-en.dev.out.de > bleu-re-basic.txt