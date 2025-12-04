Generated at 2025-12-04
## TD-1872: Create auto-changelog application

We need an application that would automatically generate a changelog out of git commits.

### Related Commits

- `585766675d51cef19ec319ef418e925cb541ddd0`: Add ChangelogReader that gets latest version from changelog
- `0cba431a235422fb73d36d2993a88dbc7089a856`: Handle incomplete and empty changelogs
- `eccbb60b38083908fe3211aa68d4df27bb876e4f`: Extract git refs with GitRepo class
- `f847bd824d0be09999011e69f45e84188b95a6d1`: Add basic ChangelogWriter
- `08c1914d99fbc7edcb730788b40b6f1322b72449`: Read changelog lines into Changelog instance
- `3f39a5529fc1c4282a015cf740ab2ff8f0fa83e8`: Optimize ChangelogReader#read function
- `c40306405ac0fbfb48c410bba5c521082ac91695`: Add explicit exception for non-matched Version
- `2e0721b090ef1a3b8a917f60336c5368c9ffdf5e`: Get file contents after certain line
- `7d749ef2723c77607203453e057a83a1dc1dc2c0`: Add extensions for git commits and collections
- `70db07ad33a0701eca7d27ea993c5097275671f4`: Construct GitLog from a git repo
- `1bb1e9ebdeb35688dc34571aa239c50bc1055c84`: Add optional predicate arg to GitRepo#constructLog fun
- `a240551f3ce3b10f53f6cd40a3f32de34dc03bcd`: Add Unreleased version object
- `48dab8471ba6c6db06b3cc677209b6f5b60a620e`: Create a changelist from git log
- `2563ab90f39d5a936a58faaee3275854fd093ec1`: Fix order of changelist entries
- `4fb4c3b8c07c103c98667f316a45a50d8728655c`: Add dir/file paths as cli options

## TD-1882: auto-changelog: duplicate entry when HEAD points to tag

The entry in the changelog is duplicated as "Unreleased" when HEAD is pointing to a git tag and previous commit is tagged as well:     * 59760ef - (2 hours ago) Fix default type of allChannelsOnExchangePoints - Mohamed Adrik (HEAD -> master, tag: v14.3.1, origin/master, origin/HEAD)
* f398bee - (3 days ago) Add option to request all channels present on exchange points - Terje Kirstihagen (tag: v14.3.0)
* d4ddb31 - (3 days ago) Add reactive channels to exchange points - Terje Kirstihagen (tag: v14.2.0)   ## [Unreleased]

- Fix default type of allChannelsOnExchangePoints

## [14.3.1]

- Fix default type of allChannelsOnExchangePoints

## [14.3.0]

- Add option to request all channels present on exchange points

### Related Commits

- `e9ef9632d7f2aae01747989872dac9472146ef99`: Fix duplicate changelog entry

## TD-1883: auto-changelog: add links for jira issue ids



### Related Commits

- `0c76acb06531728c52102038605c364b4932bb10`: Add links for jira issues

## TD-1886: auto-changelog: append to existing changelog



### Related Commits

- `6672c40a325661d1e8a9ee3d83071e2bb5e49a92`: Add possibility to append to existing changelog
- `df7fadc3b8be1c50f7ecf78910341b0affda4cff`: Add cli option for custom input filename

## TD-1884: auto-changelog: exclude commits that don't have jira issues



### Related Commits

- `8dee118f113ece5ab3db6db2ffc3200deb90c45f`: Use only jira-linked commits

## TD-1888: auto-changelog: fails if custom output dir does not exist



### Related Commits

- `d49fff106d6a072d8125947d1f678212e46c9190`: Fix handling of non-existent output dir/file

## TD-2365: Publish release notes for deployed components

We want to be able to see which Jira tickets went into a particular release. For this we could utilize the fact that we can query deployed components versions from db (probably nicer to have an API instead of direct db queries). This, paired with auto-changelog app, can give us the possibility to construct "release notes" on the fly for any given component.

### Related Commits

- `91fc90f1fa45d2f12bba4cd15f14af91e0dc1fe8`: Change project structure to multi-module

## TD-2858: Fix issues with publish docs build step failing 

The repos have a lot for tags making the publish docs fail.  Example this build from ElhubUIWebPortal15min:  https://teamcity.elhub.cloud/buildConfiguration/Elhub15Min_ElhubUiWebPortal15min_PublishDocs/61194?hideProblemsFromDependencies=false&hideTestsFromDependencies=false&expandBuildChangesSection=true&showLog=61194_125_125&expandBuildProblemsSection=true&logView=flowAware Error message in team city: `[tag] is not compliant with semantic release rules`

### Related Commits

- `16fd697e9afa381a07d945b5ea33d7833a4af131`: Fix for repos containing non-semver compliant tags

## TD-3098: Create Elhub Gradle Plugins

Replace elhub-gradle with elhub-gradle-plugins

### Related Commits

- `fd0c8de767aa9f1f4e6f0ee092f94fd3c0e742d1`: Add new elhub gradle plugins

## TD-3657: Update devxp projects

Updating the DevXP software projects so that they are up to date with use of Dependency catalogs, OWASP, sonar, etc. Eat your own dogfood.  Must use gradle-plugins Must use dependency catalogs Units tests must run and code coverage recorded in sonar.elhub.cloud OWASP dependency check running and recorded in sonar.elhub.cloud

### Related Commits

- `4659064cd3f64eb4d37704820169f6ba6969ffa0`: Update dependencies.
- `fbcd0f599ca856a393ce6ebfa9c4c2c388bf8a4c`: Update no.elhub.devxp.kotlin-core to 0.0.15.
- `709efa6b0dc8db92accc9c7a8d51afb34e8280ed`: Upgrade to OpenJDK 17.
- `2957484e430a4d94339ba773b83131920e2c5656`: Update pom.xml to 0.50.2.
- `ea32c45695e030e3aef2c8f45e392eae5ffc203f`: Update kotlin-core to 0.1.0.
- `8c40edca269d0ea298a4d3c14c50efce4a0505b5`: Add dependency check report.
- `c87ccb9a8a5575cd387e16c7e3f371d097eef174`: Upgrade devxp-gradle-plugin to 0.1.2.

## TD-3959: Release notes not working 

After auto release, changes should be published to docs.elhub.cloud. Seems to be not working currently

### Related Commits

- `fa32395c10932ab1ccf0823e0a44e3200d1a886d`: Fix gradle publishing config
- `7095c5ad6dbb5a145e0919c70f7d887eca86ec12`: Fix missing entries in ansible projects that use devxp-auto-release

## TDX-811: Update and test projects using vault connection with devxp-build-configuration version made for teamcity 2024.12 upgrade



### Related Commits

- `f13fe0137c9c239cfc66da4fc1a238a2dabf586b`: Bump devxp-build-config version for tc upgrade

## TDX-888: Generate changelogs for ShareBear on succesful prod deployment

ShareBear wants to be able to generate changelogs on successful deployments of  data-sharing-energy-data  so that they can use  data-sharing-ui  to push them to  api.elhub.no . The changelogs should be generated from changes made to  data-sharing-energy-data . As discussed in the sync on 3/5/25, we have agreed to proceed with the following flow: data-sharing-energy-data  is deployed to production A custom step in TeamCity/ArgoCD (will be Argo as SB is moving their repos to Argo in the near future) does the following: Pulls down the  data-sharing-energy-data  repository Uses functionality based on  devxp-auto-changelog  to generate a list of changes from the previous production deployment to this one Creates a pull-request to  data-sharing-ui  with a JSON (or other program-readable format) containing the list of changes The code in  data-sharing-ui  picks up this file and does front-end magic to make appear on  api.elhub.no What changes should go out to the public facing  api.elhub.no ? We have decided to go with an ‘opt-in' approach, which means that only commits which contain a specific tag/keyword (for now we will demo it with  [public ]) will be included in the changelog generated in step 2b above. If we want to remove commits with were erroneously tagged with  [public]  this can also be done in the pull request created in 2c.

### Related Commits

- `afb9e99aac8fcd52271f69f00c495ae665cca6c6`: [TDX-888] dummy commit for testing
- `b9a3597b2641040326462248884d72c68e927352`: TDX-888 another dummy commit for testing

## TDX-1051: Enable Compass in DevXP, Auth and Kubernetes projects



### Related Commits

- `5375a9c24d45c674362495a0e680134cb937f9be`: Update build and metrics tools
- `a4e6ac52aca3e049c8748d34318f83528613f5f8`: Ensure both modules are scanned in Sonarqube
- `64178e50a04aceb77ec1f981c722ee05aac7a7ab`: Add code coverage tests
- `3909aad6c5c1369040a143853f739618ef0ce085`: Fix the gradle setup

## Commits without associated JIRA issues

- `56a3cf477b5aaa5c341de9169297e9d07bc99997`: Prepend new content before existing release in changelog
- `a56fd286d0225c4d21662bda2599a6706997b64e`: Add test for ChangelogReader
- `e2c57d85f88b4644b5b575e31e57971cc2f9fce8`: Add extension to get lines from file until predicate
- `979987230eafe216042ae2b29a1841ea1ead07c6`: Refactor ChangelogWrite#writeToString fun to use Changelist
- `98527413b44637c7ebf2bad734503b8d5b5b2bec`: Implement writing different change categories from the Changelist
- `e5ef52669213ca614e77dc2a854ed91289cff0f6`: Fix order in the generated Changelist
- `c8b3b426dbd9950b971dc85254d7913a3a9df999`: Create Changelist with correct change categories
- `77d03e4459194a7ca167c394d2ee558f253bfe9f`: Add basic commandline interface implementation
- `a15653e7132d6ee0ae499c71eecff68d9a9c2521`: Add jira issue IDs to change entries
- `e5ad13da61e211118894ec09abd04b2f4501729e`: Add teamcity configuration
- `e9f0a3f9236ffb4ee1b362aa6c6854c331a81e5d`: Release first pre-stable version
- `1c63eec121aa968648ead4a4d37471ed60f1a87c`: Add vcs trigger to AutoRelease build config
- `48879465c9ecce4e1d315855212c64811ca68043`: Release with github actions
- `35f9b25820d22e15fe57868546ba160d69109b1d`: Revert "Release with github actions"
- `a7a8c60113b7c0ec0a28f365a3f437afec54d387`: Update .arcconfig
- `813e3ae9d22b058abf58f95b7941b146f2b7ed57`: Change 'unknown' title keyword to 'other'
- `76f812b2071896f9a46efeedd063ecfc7343c0f5`: Rename to devxp-auto-changelog
- `31d6adf8030375c75611f3a911a920e7cf75f46f`: Fix import in teamcity settings
- `8a3911f578be10642dd62dce4138713636bc4fa7`: Fix PublishDocs initialization in teamcity settings
- `3783563cdb915ed7c32afda00514354bb3634d81`: Fix PublishDocs dependency in teamcity settings
- `5960c4106fc2c67937ddd9bcb02870e9f4d33fa1`: Remove commit trigger
- `d3aa590e9fdf140ed4380a04104c51a1660f03d4`: TeamCity change in 'DevXP / devxp-auto-changelog' project: general settings of 'Publish Docs' build configuration were updated
- `630ea06765a4954ae90e4895cc4f0a188c4ac1bc`: Update elhub-gradle and kotlin versions
- `8d0cc9e84fdf7bded553ddf8006d3eadf9816347`: Remove TC-generated patches to build configs
- `8278b9c140f3f14e1bb92c5d7526337cc4b4461d`: Add missing license
- `43cfeee43b9cccbdc922d4d57fffb2c05c819644`: Chore - update dependencies
- `b36e72135e77c4b1d7442cff399a05b4775804ac`: Disable allure
- `59459b718806e2153f652d63bbac6fc6a8ac4c4e`: Fix failing AutoRelease build
- `099b369f6be9135d521b3c92907c5ce92190f6fb`: Fix artifacts names and publishing config
- `640b3e06ef0ebeb1f410f1a220871200214b1cd3`: Fix published artifact ids
- `a33a3c0a3c7224a167a66bf1abbb5a3c68d9ad78`: Update TeamCity settings to version 0.41.0
- `59f8e4c93ba6578cbb6804c83dd9a1d216d47b2a`: Fix broken build
- `a9e006020f69fe868401ddadce8ce3a0618f02ce`: Merge branch 'main' of github.com:elhub/devxp-auto-changelog
- `4d21b5de2774018d55cbee9b1798c74df1fdcae7`: Fix common references
- `52105ba3a983125990f025d8c26b6f803fab0dc1`: Add VCS trigger
- `ee1776a0a2f506aec2bf670b9850f717e074122b`: TeamCity change in 'DevXP / devxp-auto-changelog' project: Project editing is enabled
- `37cb7b9069889c893f699b75965e88ad0da743d1`: Update gradle to use elhub-gradle
- `26de0488e7e6c45be4b014d6aca3fc89633fdb92`: Merge branch 'main' of github.com:elhub/devxp-auto-changelog
- `dd82a32dc845eacbb5663c5f0052a1e0ac05a194`: Remove TC patch
- `c4b7917413d9ab2bac233eea2bf3d94369495955`: Remove allure report in TC
- `2f7b756285a2973cbfff7150dd632f646305b47b`: Set up sonar for multiple modules
- `bd278d18b2004c4528eedb69479ef108223106c0`: Add sonar tests
- `0969fae997a7da2e404f2be52f19b79881eabcda`: Updated elhub-gradle and use modules
- `32774b651e4f769cd14c2837a3770cfa043acd02`: Add tests to cli of AutoChangelog
- `827f1e08ccda444d2558ab7963df482725941d99`: Remove empty docs folder
- `a00e4efdf7c3a927564661e6fb5483ec4eb67cd2`: Update TC build config
- `51d746c7313bb2cbb0de441fb42d27fc0f1c3bc8`: Generate compare url for latest release
- `9fb314758c35857d6b43688c4c4974bdf6efad0a`: Update of settings by devxp-buildbot
- `56eb22437eb88d23e51a38ab724cbffdadc738f7`: Update of settings by devxp-buildbot
- `a3d152ea089b7dfad1f462f35b384dc6fe0279b3`: Update of settings by devxp-buildbot
- `6d6acfe4f3187700b7a8e5289495483f33f6da87`: Fix breaking tests
- `b0abaaeef50b8c124095fb6c7ebad2e1069527fc`: Update libraries with dependency catalogs + JDK17
- `3eafbae477540ddbfcb808112c1c070174256f0e`: Set java target to 1.8
- `546cea7be65336e23da2ff33fa5f925c7d925029`: TeamCity change in 'DevXP' project: bulk pause/activate with comment: Paused due to migration to teamcity1.
- `aa711042e58828bd4259eee9edde42d0d539272f`: Update TC settings for teamcity1
- `0ef8dd21c7429e95eddc05f069ddea240d5dc616`: Merge branch 'main' of github.com:elhub/devxp-auto-changelog
- `2536047ce17af2784a064ee0e653566c9f6e4098`: Remove pausing
- `05506bbf3d3c4683854856070ca7239d6c0c265d`: Fix ID in TC settings
- `3ce8280c858da1e8a0f0b6fd17238160a8b0d9ee`: Fix breaking TC settings
- `17359468d90a3cdedb231164af04888619f2a319`: Fix the default project files and clean out phabricator legacy (#1)
- `48345152117677290d57cce94f0fbe98b8cb06f4`: Update title keywords to support conventional commits
- `be717c2928b1a4366aae327e8f502ceb6fd123d5`: chore: bump build config version
- `21ddf7555537d4765cc4591996ec03b9443e8db3`: Update gradle versions and clean up structure [patch]
- `51214ac942121c85f19cec5bbbf4058de8477626`: Make root project a library to allow jfrog publishing [patch]
- `8b21ed517af2bf84673ce5e6313013081fb071da`: Refactor the gradle structure again [patch]
- `f98fa4ff51f5f2d7197042a3f27902b0c11c31f1`: Remove artifactoryRepository param from tc pipeline [patch]
- `78d0edb38d93cb4c55fc921023c58ef834d3ccee`: Refactor multi-module setup again [patch]
- `82bb30a5e56245734a6ff166777d605615cb4010`: Update build-config version
- `944d5b53e45bd3d7b14e13edc583122eefaec6a9`: Add changelog file to make docs generate changelog (for testing)
- `8632c3008409128c1e500a4a2cdd27c537f2b768`: Move .changelog to .devxp folder
- `b73399df1075a5b29faa67069ecfc213b9394be2`: Update to match new jira
- `ae0f51cfea80c9482ce471c1c900912ebeb29327`: Update README
- `1e954638650250a486e33fb1f86bbcb2d9cea567`: Update to use shadowJar
- `56a29d9feafc298e55f61b8756b766d6de871631`: Enable Renovate scanning for this repo
- `a10802f208e915bfaf6466a00fc64898bbba9fa1`: Add option to generate changelog based on url [minor]
- `3422f2f124dd10a707f1417c2fd008a2825c763c`: Merge pull request #19 from elhub/feat/add-renovate-config
- `33cee0f66346a2921a2f9346171aebe46a7e7ce9`: Fix errors in publishing configuration
- `e83a5070565132452af23497b9e6353dfe38c98d`: Add support for generating changelog in json format [minor] (#23)
- `4c87863ce3fd66674f94e63186732d9a1b213963`: Refactor to use FunSpec
- `4b898e6748c438ce9c38eb96fcb5531f6a55da29`: Commit changelog-file for use in testing
- `7f10435cd883f90ddac012e20f5f25a53e0985fb`: Remove changelog-file added in previous commit
- `b68e0f00e8dfdaac2b170e0c0c546b0943a98dd0`: Update to use this repo for testing instead of old bitbucket repo
- `67ce3a23995af1782e1908b0f397b4a1952e46b4`: Fix if conditions and replace with when
- `205efe2afc3eca81cd347da8060dac3d7a336985`: Refactor test setup and add tests for json to ChangelogWriterTest
- `d7736e943e3a6342642b32562ce042688f15a77c`: Update Minor or patch updates (#21)
- `3654762d9e7f18e5887480b001f3abb09a1078e1`: Add more cli tests
- `36eaa772be491d31fb0a47fe87fc24e112558c9e`: Add jira and tag functionality [patch] (#36)
- `c2fcf08a7fece41b335664f033304f5899c9bfe7`: Update Minor updates [minor] (#38)
- `ed147351dfa5a0c258366b89ba01521f1ef382f5`: fix: update tag filtering to find previous similar tag
- `4123f7559d2abafee18672a3f8f3d3baa92b3840`: Bump version [minor]
- `fb04b0eafe756f5947ef4d5b3fd7e205daaae43a`: refactor: combine input and output changelog name params [minor]
- `58bf82dd0a04fe428cb23adb6eef3954d7763b84`: fix: correct tag sorting [patch]
- `7c1fbf1da0b709b52d79df2f89bba8731e57b8ff`: fix: ensure newline for json changelog [patch]
- `5b49e09835c4cf0d20d7343956d779d5ad66336a`: refactor: update to not use version catalog [patch]
- `3cbd1b767353aaf91138810ba66872f384b56409`: fix(deps): update dependency org.eclipse.jgit:org.eclipse.jgit.ssh.jsch to v7 (#46)
- `5d1e52f56d8a4a5ddc2d18ec772880b24417cdcb`: fix(deps): update dependency org.eclipse.jgit:org.eclipse.jgit to v7 [patch] (#45)
- `76096303b6e8fdba869ab76d5143be9879f524aa`: fix(deps): update patch updates (#37)
- `0a94533c73b310e96f8961c8f085d5db9aba89ad`: chore(deps): update plugin org.jetbrains.kotlin.plugin.serialization to v2.1.21 [patch] (#48)
- `31ab7d97e925224e4aa617cdd0e35d228d810ab2`: fix(deps): update minor updates [patch] (#47)
- `f7dc800834b5f3e0ee46f56adc336476f1889ac3`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.36.8 (#49)
- `e6d899b5315c558fe0b5d5583a2876f68d454254`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.36.9 (#50)
- `2e72f29dd4e1d42b4df3c1b86aa763642407881d`: chore(deps): update plugin no.elhub.devxp.kotlin-core to v0.7.0 (#51)
- `8db853f8bebc5c0b81af9fd7365538ff8e4c027d`: fix(deps): update patch updates (#52)
- `67ce0c0a16e3cf09ccd9f344746df6a46ade5ec8`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.38.0 (#53)
- `147deb8522d20263ad0c172441db75fc03f60fbf`: fix(deps): update patch updates (#54)
- `7018adabe677dca8c8342fc21f909ac8c16e439f`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.38.14 (#55)
- `83b9c6c4ba9ec82a457f6a465e2f56120c195df2`: chore(deps): update dependency gradle to v8.14.2 (#57)
- `e6960067e0d5586fe2ab7559bd9a418bc7d92b28`: fix(deps): update minor updates (#56)
- `c77d2fb1db0a6ae6ec5b2003b398301873b2f390`: chore(deps): update plugin no.elhub.devxp.kotlin-core to v0.7.4 [patch] (#58)
- `e7aa24cfa485fc145678b4a0160a22a2e8714cdb`: Add date and time vars to json [minor] (#59)
- `7e5941a6505b5deefea4bf21b28bf241d68f6822`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.10 (#61)
- `841b19e997bf366d13111fb01ddac98ea9c35c0d`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.12 (#62)
- `fe92bdd8b53f19d7418f3d195f1e1c08dfa7863b`: fix(deps): update patch updates [patch] (#60)
- `4abda07e0999b087e74bbaa055cad2b993c0794a`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.13 (#64)
- `af726b03adb27aee2aae2cecc1b27caa5bfd9150`: fix(deps): update minor updates [patch] (#63)
- `f6ba0178d5309a1490fc6fdaf9c9e47fde4c5603`: fix(deps): update patch updates [patch] (#65)
- `fa62d4309c8eee87848372b00111983f912072e2`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.16 (#67)
- `46f406877bf3398fe2ac404ef81a310f6fe5f99e`: chore(deps): update patch updates [patch] (#66)
- `c8e7ed3e3dd78dd9124c0171df3bfb0e23dc6c0b`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.17 (#69)
- `75e9f8d952aaa5068ef8d5e27b8aa2bd7fa96026`: fix(deps): update dependency commons-io:commons-io to v2.20.0 [patch] (#70)
- `1e9727635a126a31015bd056e29ba28ef9715d84`: fix(deps): update patch updates [patch] (#68)
- `0ab45f94e7244a070d9c68dbc1141607260e7c91`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.21 (#73)
- `d45eac791336c756701d6362f677b598b42fdf3e`: chore(deps): update plugin no.elhub.devxp.kotlin-core to v0.7.10 [patch] (#72)
- `1d89815a39a52f00d7f26354c66948a954b2c735`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.22 (#75)
- `7d45767b9bc94eb8ce6192bf7c0339690b26aef6`: chore(deps): update dependency gradle to v9 [patch] (#76)
- `88a93c1ace6fd09e79e836ff7bb32be548bb442f`: remove artifacts-clear step
- `d68dcdbcf7d3ca730f71ab9c25debd0c7e7a0602`: add afterEvaluate to publishing block
- `9267926c835dc65a0b170beca5bf09b1da527b97`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.24 (#77)
- `93de5c73913008f80b661aceeec5c9a94a3d8da7`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.26 (#78)
- `41906fb65f2c19fecced89051816b913e22175bf`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.28 (#80)
- `3b8c43c7662bc18b4b4b181b8920c4ba7850fa0b`: chore(deps): update plugin org.jetbrains.kotlin.plugin.serialization to v2.2.10 [patch] (#79)
- `5270aac6fdcce848e9cfa826a386091cce0512d3`: chore(deps): update plugin com.jfrog.artifactory to v6 [patch] (#74)
- `1f13c5ebbef7c8c747d0105b08a53b2acc65497f`: Fix gradle wrapper
- `15c8e11c9780901125c4cc54cea3596679a64fa0`: Update dependencies
- `2925a9b75a539adb4132c0408c25f28d206817a3`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.40.33 (#82)
- `d91f9f097afef682a17d542475835122f3656425`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.43.0 (#89)
- `3a1bd66a45de76b1e9d603ac65aaa40c66609365`: chore(deps): update dependency org.jetbrains.kotlin.plugin.serialization to v2.2.10 [patch] (#88)
- `c80c58cfc37913ab13b25774ce78125d9f068974`: fix(deps): update patch updates to v6.0.2 [patch] (#90)
- `9df1aac29c1fe8ae5d47f63c03ad0e0b6599f8ae`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.44.1 (#91)
- `6bc55e3511d0a025fdae3550cd46d09269a3a43f`: feat: replace autoRelease with publish (#92)
- `e7a200e2150a3f69d8721f59248ecf56f3011aa9`: test: dummy commit for publish job [patch]
- `81188b9e4cbbf2d1a508c97120c1596e8a8750a2`: fix: remove workingDir from gradlePublish
- `dd387166afcf1443552d65efbd0ecd26722f7169`: fix: skip publishing for core [patch]
- `99f3e67b145a7a229e5c8cca0d4e2492faae6458`: chore: bump build configuration version
- `3e82777df6cae080602cfd96acbd2956a193ca3c`: chore: bump version [patch]
- `04ead990d44400e4d7f57698d54de76d95b595de`: chore: bump version again [patch]
- `be294885ef3a1fc96ee040da371c73ec2281757f`: fix(deps): update patch updates [patch] (#94)
- `2e3064c84918a948233df50b30353a04ee9bbbfc`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.44.7 (#93)
- `5b89555ef39d52afb2f03d40f9d5ba52ec679f2c`: chore: bump version again [patch]
- `421eac757b6ac8d7610409016869f1274ee27df0`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.46.2 (#95)
- `7374a2a73083cb470d9f0345ff51d1321223413f`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.47.3 (#96)
- `b3be821f56ba238170906a7c8d76c29997ca45d1`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.47.7 (#98)
- `435c55f2ed6b9526127438c566dab6db8b0417a1`: chore(deps): update dependency gradle to v9.1.0 [patch] (#97)
- `5b5138adccf602ca4c934413afdf0b1a455e8476`: fix(deps): update dependency io.mockk:mockk to v1.14.6 [patch] (#101)
- `6a299dbd7fbb5705caa2105a3bfb71e2f25f4b6e`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.47.10 (#102)
- `970c1a05d6941450eb2a0b61e1009c8bcc9ffe1e`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.47.12 (#104)
- `52889d29b9e7eb5d87d2e8e36b8a514001c90cf5`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.47.16 (#105)
- `df76f5e0781c554778378da34efb03e2368bc4cd`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.47.18 (#109)
- `cf724ae0469407056780c85d57ef9ff834f8cd16`: Add new tests
- `cd82945943ac9be2115ff4be49fa5c76cea7eb79`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.51.0 (#111)
- `7a4e5c5c34653682af9a13714788efd2bac181c0`: fix(deps): update dependency no.elhub.devxp:devxp-build-configuration to v1.51.2 (#113)
- `deceb5544db19fd8f2eb9a69c6f58263969facb5`: rewrite: full rewrite of auto-changelog
- `8b50afd56256c03448160cd3d7faf1818c39c20b`: add more tests
- `9d833f38df0dbb114640573a18d3464983ddc375`: add jira integration
- `b29bc763fed2894ef96d68f81320d8ded982b16f`: add tests for jira integration
- `8b5922bf04c10cf12df447e97dbce0520a41a74b`: add check for env vars
- `aaf0285840c7f8c111208d81a87ac1887977a08d`: restructure folders
- `07c7dc9bfc1a0d19724b33cf36f11d04923efc87`: add markdown writer
- `d764a3835f2cf5cd8306609036bb36a9c6e9c102`: refactor test structure
- `02d70455d5c98e767c3a5807ec5188368bee6bd5`: add tests for markdownwriter
- `3de66a9a9f8a91a8b59755cd45374dbfbb3677ad`: add cli flags
