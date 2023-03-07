# DavinciSQL

TL;DR natural language to SQL using OpenAI Davinci  

## Sample result

**Asking the question**  

**Create a SQL request to find the  10 countries with the highest total goal**

Against the CSV file most_funded_feb_2023.csv, will lead to this SQL being generated

SELECT country, SUM(goal) AS total_goal\
FROM playground.most_funded_feb_2023\
GROUP BY country\
ORDER BY total_goal DESC\
LIMIT 10;

When we execute the SQL we get 

country			total_goal\
JP			103000000\
US			28924321\
HK			2422791\
GB			1806999\
FR			500000\
PL			325000\
DK			300223\
CA			203000\
CH			172000\
ES			130000

## Join Example:


**Given the tables playground.products, and playground.product_sales. Where The playground.products table has the columns id, name, and the playground.product_sales table has the columns product_id, units_sold, and price_per_unit generate SQL query to select the top 10 product names with the highest total sales answer only in sql** 



======= Generated Query  ====== 

SELECT p.name, SUM(s.units_sold * s.price_per_unit) AS total_sales\
FROM playground.products p\
INNER JOIN playground.product_sales s\
ON p.id = s.product_id\
GROUP BY p.name\
ORDER BY total_sales DESC\
LIMIT 10;


Answer: 

name			  total_sales\
iphone  			83000.0\
samsung	  		6000.0\
android		  	4000.0
