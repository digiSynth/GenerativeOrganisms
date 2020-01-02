GOMovingSystemWidget{

	var messagesCount = 0;
	var labelStaticText;
	var labelOrganisms, labelConsole;
	var startButton, pauseResumeButton;
	var <window, compositeView, staticText;
	var <system;

	var toLog, logRoutine;

	var toRemove, removalRoutine;

	var dictionary;

	*new{ |organismSystem|
		^super.new.pr_InitGOMovingSystemWidget(organismSystem);
	}

	system_{|newSystem|

		if(system.class!= GOMovingSystem){
			Error("This field can only accept an instance of GOMovingSystem");
		};

		system = newSystem;
	}

	pr_InitGOMovingSystemWidget{|input|

		system = input;

		toLog = toLog ? List.new;
		toRemove = toRemove ? List.new;

		dictionary = dictionary ? IdentityDictionary.new;

		window = Window.new("GOMovingSystem Widget",
			Rect(rrand(50, 500), rrand(500, 1000),
				750, 750
			),
			scroll: true
		).background_(Color.white).front
		.alwaysOnTop_(true);

		startButton = Button(window, window.view.bounds)
		.states_([
			["Start system", Color.green.blend(Color.black, 1/3)]
		])
		.font_(Font("Monaco", 36))
		.action_({|button|

			defer{button.remove};

			labelOrganisms = StaticText(window, Rect(0, 0,

				window.view.bounds.width / 2,
				100

			))
			.font_(Font("Monaco", 16))
			.align_(\center)
			.string_("Active organisms");

			compositeView = CompositeView(window, Rect(
				25, 75,

				window.view.bounds.width / 2 * 0.95,
				window.view.bounds.height - 105

			));

			labelConsole = StaticText(window, Rect(
				window.view.bounds.width / 2, 0,

				window.view.bounds.width / 2,
				100

			))
			.font_(Font("Monaco", 16))
			.align_(\center)
			.string_("Console log");

			staticText = StaticText(window, Rect(
				window.view.bounds.width / 2, 75,

				window.view.bounds.width / 2 * 0.95,
				window.view.bounds.height - 105

			))
			.align_(\topLeft)
			.background_(Color.grey.blend(Color.white, 1/3))
			.font_(Font("Monaco", 14))
			.string_("");


			compositeView.decorator =
			FlowLayout(compositeView.bounds, 5@5, 5@5);

			system.start;

		});

		window.onClose = {

			system.free;

		};

	}

	registerOrganism{|key|

		if(dictionary[key].isNil){
			dictionary.add(key -> this.pr_MakeButton(key));
		};

	}

	removeAt{|key|

		this.pr_RunRemovalTask(key);

	}

	pr_RunRemovalTask{|key|

		if(removalRoutine.isPlaying){

			removalRoutine.stop;

		};

		toRemove.add(key);

		removalRoutine = Routine({

			if(toRemove.isEmpty.not){

				toRemove.copy.size.do{

					var key = toRemove.removeAt(0);

					dictionary.removeAt(key).value.remove;

					(1/24).wait;
				};

			};

		});

		removalRoutine.play(AppClock);
	}


	log{|message|

		toLog = toLog ? List.new;

		this.pr_RunLogTask(message);

	}

	pr_RunLogTask{|message|

		if(logRoutine.isPlaying){

			logRoutine.stop;

		};

		toLog.add(message);

		logRoutine = Routine({

			if(toLog.isEmpty.not){

				toLog.copy.size.do{

					if(messagesCount==0){

						staticText.string = "";

					};

					staticText.string = staticText.string
					++"\n"++(toLog.removeAt(0));

					messagesCount = messagesCount + 1 % 24;

					(1/48).wait;
				};

			};

		});

		logRoutine.play(AppClock);
	}

	pr_MakeButton{ |key|
		var return =  `nil;

		defer{
			var toReturn = Button(compositeView, 18@18)
			.states_([
				[key.asString, Color.red, Color.grey.blend(Color.white, 1/3)]
			])
			.font_(Font("Monaco", 6))
			.acceptsMouse_(false);

			return.value = toReturn;
		};

		^return;

	}



}