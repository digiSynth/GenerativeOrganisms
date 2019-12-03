GOBlock[]{
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

	pr_AddToBlock{|input|
		if(block.size < this.class.maxSize){
			block = block.add(input);
		}
	}

	*newRand{
		var return;
		var block = GOBlock.new;

		maxSize.do{|item|

			var segs = rrand(4, 24);
			var env = Env(
				levels: ({1.0.rand}!segs),
				times: ({exprand(1e-3, 1.0)}!(segs-1)).normalizeSum,
				curve: ({12.0.bilinrand}!(segs-1))
			);

			block = block.add(GOCurve(env));
		};
		return = block;
		^return;
	}

	*newClear{ |n = 1|
		var return = GOBlock.new;
		n.do{|item|
			return = return.add(GOCurve.new);
		}
	}

	*fill{|n, function|
		var return = GOBlock.new;
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

		if(input.class == GOCurve){
			ogcurve = input;
		}/*ELSE*/{

			case
			{input.isCollection}{

				case
				{input.size==1}{
					if(input[0].class==Env){
						ogcurve = GOCurve(input[0]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				}

				{input.size==2}{
					if(input[0].class==Env){
						ogcurve = GOCurve(input[0], input[1]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				}

				{input.size==3}{
					if(input[0].class==Env){
						ogcurve = GOCurve(input[0], input[1], input[2]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				}

				{input.size > 3}{
					if(input[0].class==Env){
						ogcurve = GOCurve(input[0], input[1], input[2]);
					}/*ELSE*/{
						this.pr_ThrowArrayMsg;
					}
				};
			}

			{input.class==Env}{
				ogcurve = GOCurve(input);
			}

			{(input.class!=Env) and: {input.isCollection.not}}{
				Error.throw("Viable inputs are only GOrganismCurve, "
					++"\n!a collection (Env, Float, Float), "
					++"\n!or an Env")
			}

		};

		if(ogcurve.class==GOCurve){
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

	//pos
	posCurve{
		if(block.size>=2){
			^block[1];
		}
	}

	//amp
	ampCurve{
		if(block.size>=3){
			^block[2];
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

	isFull{
		^(this.size==this.class.maxSize);
	}

	averageBlocks{|otherBlock|
		var returnBlock = GOBlock.new;

		if(otherBlock.class!=GOBlock){
			Error("Can only average a GOBlock with other GOBlocks.").throw;
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