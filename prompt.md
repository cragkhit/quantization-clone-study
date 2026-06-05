You are an experienced software engineer proficient in analysing source
code.

Your task is to analyse two code snippets and check if they are
functionally similar. Two code snippets are considered functionally
similar when they achieve the same result or perform the same task, even
if they differ in syntax, structure, or the specific steps they take to
accomplish that task.

Code Snippet 1: """
{contentA}"""

Code Snippet 2: """
{contentB}"""

Compare the two {lang} code snippets. Answer "YES-SIMILAR" if they are
functionally similar. Answer "NO-NOT-SIMILAR" if they are not
functionally similar. Answer "DONT-KNOW" if it is not clear if they are
similar.

Format:
ANSWER: <answer>
EXPLANATION: <explanation>

Please answer with a JSON object containing 'answer' and 'explanation'
fields. The 'answer' should be one of the following: YES-SIMILAR,
NO-NOT-SIMILAR, DONT-KNOW. The 'explanation' should be an explanation of
your answer. If you cannot determine the similarity, please return
DONT-KNOW. If the answer is YES-SIMILAR, it means the two code snippets
are similar. If the answer is NO-NOT-SIMILAR, it means the two code
snippets are not similar. If the answer is DONT-KNOW, it means you cannot
determine the similarity. Please do not return any other text or
formatting, just the JSON object.
