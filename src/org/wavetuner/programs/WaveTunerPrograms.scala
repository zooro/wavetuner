package org.wavetuner.programs

import java.lang.Runnable
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.{ List => JList }
import java.util.Map
import org.wavetuner.feedback.audio.SoundPlayer
import org.wavetuner.eeg.EegMeasurementSeries
import org.wavetuner.eeg.MeasurementSeries
import org.wavetuner.eeg.Measurement
import org.wavetuner.feedback.Feedback
import org.wavetuner.feedback.audio.AudioFeedback
import org.wavetuner.R
import org.wavetuner.EegChannels
import org.wavetuner.eeg.MockMeasurementSeries
import org.wavetuner.programs.evaluations.AlphaThetaEvaluation
import org.wavetuner.programs.evaluations.AttentionMeditationEvaluation
import org.wavetuner.programs.evaluations.SimpleAlphaThetaProgram
import org.wavetuner.programs.evaluations.SingleValueRewardEvaluation
import org.wavetuner.programs.evaluations.Evaluation
import scala.collection.JavaConversions._
import scala.collection.immutable.ListMap

object WaveTunerPrograms {
  import R._
  import R.raw._
  import EegChannels._
  var programsByName: Map[String, NeuroFeedbackProgram] = _
  def programs: JList[NeuroFeedbackProgram] = new ArrayList(programsByName.values)
  def programNames: JList[String]  = new ArrayList(programsByName.keySet())
  var measurement: MeasurementSeries = new EegMeasurementSeries
  def initialize(soundPlayer: SoundPlayer) {
    programsByName = programs(soundPlayer)
  }

  def programs(soundPlayer: SoundPlayer): Map[String, NeuroFeedbackProgram] = {
    val defaultSoundMap = scala.collection.immutable.Map(bonus -> sound_bell)
    val oneValueAudioFeedback = new AudioFeedback(soundPlayer, defaultSoundMap + (standard -> sound_unity))
    oneValueAudioFeedback.constantFeedbackOn(standard)
    val programs = List(
      new NeuroFeedbackProgram(new AttentionMeditationEvaluation, measurement, new AudioFeedback(soundPlayer, defaultSoundMap + (meditationChannel -> sound_ocean, attentionChannel -> sound_unity), attentionChannel, meditationChannel)),
      new NeuroFeedbackProgram(new AlphaThetaEvaluation, measurement, new AudioFeedback(soundPlayer, defaultSoundMap + (lowAlphaChannel -> sound_ocean, thetaChannel -> sound_unity, lowBetaChannel -> sound_brooks), lowAlphaChannel, thetaChannel, lowBetaChannel)),
      new NeuroFeedbackProgram(new SimpleAlphaThetaProgram, measurement, new AudioFeedback(soundPlayer, defaultSoundMap + (lowAlphaChannel -> sound_ocean, thetaChannel -> sound_unity, lowBetaChannel -> sound_brooks), lowAlphaChannel, thetaChannel, lowBetaChannel))) ++ List(
        new SingleValueRewardEvaluation(_.meditationMeasure, "Meditation"),
        new SingleValueRewardEvaluation(_.attentionMeasure, "Attention"),
        new SingleValueRewardEvaluation(_.midGammaMeasure, "Mid Gamma"),
        new SingleValueRewardEvaluation(_.lowGammaMeasure, "Low Gamma"),
        new SingleValueRewardEvaluation(_.highBetaMeasure, "High Beta"),
        new SingleValueRewardEvaluation(_.lowBetaMeasure, "Low Beta"),
        new SingleValueRewardEvaluation(_.highAlphaMeasure, "High Alpha"),
        new SingleValueRewardEvaluation(_.lowAlphaMeasure, "Low Alpha"),
        new SingleValueRewardEvaluation(_.thetaMeasure, "Theta"),
        new SingleValueRewardEvaluation(_.deltaMeasure, "Delta")).map(evaluation => new NeuroFeedbackProgram(evaluation, measurement, oneValueAudioFeedback))
    new LinkedHashMap[String, NeuroFeedbackProgram](ListMap.empty ++ programs.map(p => (p.toString, p)))
  }
}
