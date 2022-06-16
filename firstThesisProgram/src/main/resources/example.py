import sys
from transformers import RobertaConfig, RobertaTokenizer, RobertaForMaskedLM, pipeline

model = RobertaForMaskedLM.from_pretrained("microsoft/codebert-base-mlm")
tokenizer = RobertaTokenizer.from_pretrained("microsoft/codebert-base-mlm")

fill_mask = pipeline('fill-mask', model=model, tokenizer=tokenizer)
outputs = fill_mask(sys.argv[1])

print(outputs)