package mg.ratra.agentique.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mg.ratra.agentique.domain.voice.RuleBasedVoiceIntentParser
import mg.ratra.agentique.domain.voice.VoiceIntentParser

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {

    @Binds
    abstract fun bindVoiceIntentParser(impl: RuleBasedVoiceIntentParser): VoiceIntentParser
}