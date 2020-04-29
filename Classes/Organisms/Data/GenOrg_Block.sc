GenOrg_Block[]{
	classvar <>maxSize = 7;
	var <block;

	*new{
		^super.new;
	}

	at{|i|
		^block[i];
	}

	removeAt{|i|
		block.removeAt(i);
	}

	size{
		^block.size;
	}

	do{ |function|
		block.do{|item, index| function.value(item, index)};
	}

	clear{
		block.copy.size.do{|item|
			block.removeAt(0);
		};
	}

	pr_AddToBlock{|input|
		if(block.size < this.class.maxSize){
			block = block.add(input);
		}
	}

	*newRand{
		var randBlock = GenOrg_Block.new;
		maxSize.do{|item|
			randBlock.add(GenOrg_Curve.newRand);
		};

		^randBlock;
	}

	*newClear{ |n = 1|
		var return = GenOrg_Block.new;
		n.do{|item|
			return = return.add(GenOrg_Curve.new);
		}
	}

	*fill{|n, function|
		var return = GenOrg_Block.new;
		var size = this.maxSize;

		if(n > size){
			format("Warning: input number exceeds max block size."
				++" Only % will be made.", size, size).postln;
		};

		n.do{|i|
			var toAdd = function.value(i);
			return.add(toAdd);
		}

		^return;
	}

	add{ |input|
		var ogcurve;

		if(input.class == GenOrg_Curve){
			ogcurve = input;
		}/*ELSE*/{

			case
			{input.isCollection}{
				case
				{input.size==1}{
					if(input[0].class==Env){
						ogcurve = GenOrg_Curve(input[0]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				}
				{input.size==2}{
					if(input[0].class==Env){
						ogcurve = GenOrg_Curve(input[0], input[1]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				}
				{input.size==3}{
					if(input[0].class==Env){
						ogcurve = GenOrg_Curve(input[0], input[1], input[2]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				}
				{input.size > 3}{
					if(input[0].class==Env){
						ogcurve = GenOrg_Curve(input[0], input[1], input[2]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				};
			}
			{input.class==Env}{
				ogcurve = GenOrg_Curve(input);
			}

			{(input.class!=Env) and: {input.isCollection.not}}{
				Error.throw("Viable inputs are only GOrganismCurve, "
					++"\n!a collection (Env, Float, Float), "
					++"\n!or an Env")
			}
		};
		if(ogcurve.class==GenOrg_Curve){
			this.pr_AddToBlock(ogcurve);
		}/*ELSE*/{
			Error("Failed to instantiate GOrganismCurve").throw;
		}
	}

	*pr_ThrowArrayMsg{
		Error("First element of Collection must be an Env").throw;
	}

	//rate
	rateCurve{
		if(block.size>=1){
			^block[0];
		}
	}

	pr_TestInput{|input|

		if(input.class!=GenOrg_Curve){
			if(input.clas==Env){
				^GenOrg_Curve(input);
			}/*ELSE*/{
				this.pr_ThrowArrayMsg;
			};
		}/*ELSE*/{
			^input
		};
	}

	rateCurve_{|toAdd|
		toAdd = this.pr_TestInput(toAdd);

		if(block.isEmpty){
			this.add(toAdd);
		}/*ELSE*/{
			block[0] = toAdd;
		};

	}

	//pos
	posCurve{
		if(block.size>=2){
			^block[1];
		}
	}

	posCurve_{|toAdd|
		toAdd = this.pr_TestInput(toAdd);

		//if a second curve has not been added yet, add it
		if(block.size==1){
			this.add(toAdd);
		};

		//if the block is empty, add a random first index before adding this one
		if(block.size < 1){
			this.add(GenOrg_Curve.newRand);
			this.add(toAdd);
		};

		//If the array is bigger than size 2, replace index 1 with the target
		if(block.size >=2){
			block[1] = toAdd;
		};

	}

	//amp
	ampCurve{
		if(block.size>=3){
			^block[2];
		}
	}

	ampCurve_{|toAdd|
		toAdd = this.pr_TestInput(toAdd);

		case
		//if a third curve has not been added yet
		{block.size==2}{
			this.add(toAdd);
		}

		//has fewer than 2 blocks, add the difference
		{block.size < 2}{

			var offset = block.size;
			(2 - offset).do{|i|
				 this.add(GenOrg_Curve.rand);
			};
			this.add(toAdd);

		}


	}

	//filter
	ffreqCurve{
		if(block.size>=4){
			^block[3]
		}
	}

	//impulserate
	impulseRateCurve{
		if(block.size>=5){
			^block[4];
		}
	}

	//grainDur
	grainDurCurve{
		if(block.size>=6){
			^block[5];
		}
	}

	//rq
	rqCurve{
		if(block.size>=6){
			^block[6];
		}
	}

	printOn{|stream|
		var string;

		if(block.isNil or: {block.isEmpty}){
			string = "";
		}/*ELSE*/{
			block.size.do{|index|
				case
				{index==0}{
					string = string++"rateCurve";
					if(block.size > 1){
						string = string++", ";
					};
				}
				{index==1}{
					string = string++"posCurve";
					if(block.size > 2){
						string = string++", ";
					};
				}
				{index==2}{
					string = string++"ampCurve";
					if(block.size > 3){
						string = string++", ";
					};
				}
				{index==3}{
					string = string++"ffreqCurve";
					if(block.size > 4){
						string = string++", ";
					};
				}
				{index==4}{
					string = string++"impulseRateCurve";
					if(block.size > 5){
						string = string++", ";
					};
				}
				{index==5}{
					string = string++"grainDurCurve";
					if(block.size > 6){
						string = string++", ";
					};
				}
				{index==6;}{
					string = string++"rqCurve";
				};
			};
		};

		stream
		<<"GenOrg_Block[ "
		<<string
		<<" ]";

	}

	isFull{
		^(this.size==this.class.maxSize);
	}

	averageBlocks{|otherBlock|
		var returnBlock = GenOrg_Block.new;

		if(otherBlock.class!=GenOrg_Block){
			Error("Can only average a GenOrg_Block with other GenOrg_Blocks.").throw;
		};

		if(this.size >= otherBlock.size){
			otherBlock.do{|item, index|
				var averageCurve = item.averageCurves(block[index]);
				returnBlock.add(averageCurve);
			};
		}/*ELSE*/{
			block.do{|item, index|
				var averageCurve = item.averageCurves(otherBlock[index]);
				returnBlock.add(averageCurve);
			}
		};

		^returnBlock;
	}
}