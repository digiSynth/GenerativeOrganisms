+ Function{
	++{|function|
		^{|...args|
			[
				this.valueArray(args),
				function.valueArray(args)
			]
		}
	}
}