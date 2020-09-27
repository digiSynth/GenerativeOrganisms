{ | modules, buffer, timescale |
	var rate = \rate.kr(
		1,
		spec: ControlSpec(0.2, 4.0, \exp)
	);

	var pos = \pos.kr(
		0.5,
		ControlSpec()
	);

	var amp = \ampDB.kr(
		-12,
		spec: ControlSpec(-36, -3)
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

	var bufdur = BufDur.kr(buf);
	var phasor = (pos * bufdur).wrap(0, bufdur);
	var impulse = Impulse.ar(impulseRate);
	var sig = TGrains.ar(
		1,
		impulse,
		buf,
		rate,
		phasor,
		grainDur
	) * 16;
	sig = BPF.ar(
		sig,
		ffreq,
		rq
	);
	sig * amp;
};