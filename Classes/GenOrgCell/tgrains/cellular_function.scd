{ | modules, buffer, timescale |
	var rate = \rate.kr(
		1,
		spec: ControlSpec(0.2, 4.0, \exp)
	);

	var pos = \pos.kr(
		0.5,
		ControlSpec(0.0, 1.0, \lin)
	);

	var amp = \ampDB.kr(
		-12,
		spec: ControlSpec(-36, -3, \lin)
	).dbamp;

	var ffreq = \ffreq.kr(
		2500,
		spec: ControlSpec(120, 16000, \exp)
	);

	var impulserate = \impulserate.kr(
		20,
		spec: ControlSpec(8.0, 128.0, \exp)
	);

	var graindur = \graindur.kr(
		0.1,
		spec: ControlSpec(0.0025, 0.2, \exp)
	);

	var rq = \rq.kr(
		0.2,
		spec: ControlSpec(0.1, 0.8, \lin)
	);
	var bufferdur = BufDur.kr(buffer);
	var phasor = (pos * bufferdur).wrap(0, bufferdur);
	var impulse = Impulse.ar(impulserate);
	var sig = TGrains.ar(
		1,
		impulse,
		buffer,
		rate,
		phasor,
		graindur
	) * 64;
	sig = BPF.ar(
		sig.tanh,
		ffreq,
		rq
	);
	sig * amp;
};
