# DavinciSQL

TL;DR natural language to SQL using OpenAI Davinci  

## Sample result

**Asking the question**  

*Create a SQL request to find the  10 countries with the highest total goal*

Against the CSV file most_funded_feb_2023.csv, will lead to this SQL being generated

SELECT country, SUM(goal) AS total_goal
FROM playground.most_funded_feb_2023
GROUP BY country
ORDER BY total_goal DESC
LIMIT 10;

When we execute the QQL we get 

country			total_goal			
JP			103000000			
US			28924321			
HK			2422791			
GB			1806999			
FR			500000			
PL			325000			
DK			300223			
CA			203000			
CH			172000			
ES			130000			

