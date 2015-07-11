## Code Review Checklist

1. Is the functionality correct? 
2. Are the classes named suitably? 
3. Are the functions named suitably? 
4. How's the datastructure being used? Is it the correct DS or it needs improvement? 
5. If it contain functions that can be reused later then are there Utils created for them? 
6. Can it use already available Util functions? 
7. Are all the input arguments being validated? 
8. How will the functionalities affect the performance of the app - time and memory? 
9. Is it absolutely necessary to run it on UI therad or would a background thread suffice? 
10. Are all the fail points handled? 
11. Does it degrade gracefully in case of unknown failures? 
12. Are the app-doing level standards being followed?  
13. Are the operations thread safe? 
14. What pieces of the component can be executed in parallel? 
15. Are the layouts suitable for all the screen dimensions? 
16. What classes would become obsolete after this implementation? 
