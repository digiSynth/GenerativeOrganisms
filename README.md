# GenOrg

GenOrg is a SuperCollider quark that makes real-time generative music using evolutionary algorithms. It was developed in partial fulfillment of the requirements for my thesis in Music Technology at NYU. 

Currently, the **main** branch of this repository represents the work that was submitted. There are some issues with it: even though it gets the job done, I would argue that it is rigid and unstable. The **dev** branch of this repository is where I am refactoring the code to make it more configureable and therefore hopefully more useful. That work, however, relies on the framework established by another quark, [CodexIan](https://github.com/ianmacdougald/CodexIan). 

## Installation
To install this quark, run the following in SuperCollider: 
~~~~
Quarks.install("https://github.com/ianmacdougald/CodexIan");
Quarks.install("https://github.com/ianmacdougald/GenOrg");
~~~~

