package mg.ratra.agentique.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mg.ratra.agentique.data.pbf.PbfRepository
import mg.ratra.agentique.domain.agents.GeomaticienAgent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindGeomaticienAgent(impl: PbfRepository): GeomaticienAgent
}
