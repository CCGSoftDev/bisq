/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.dao.governance.ballot;

import bisq.core.app.BisqEnvironment;
import bisq.core.dao.DaoSetupService;
import bisq.core.dao.governance.period.PeriodService;
import bisq.core.dao.governance.proposal.ProposalService;
import bisq.core.dao.governance.proposal.ProposalValidator;
import bisq.core.dao.governance.proposal.storage.appendonly.ProposalPayload;
import bisq.core.dao.state.model.governance.Ballot;
import bisq.core.dao.state.model.governance.BallotList;
import bisq.core.dao.state.model.governance.Vote;

import bisq.common.proto.persistable.PersistedDataHost;
import bisq.common.storage.Storage;

import javax.inject.Inject;

import javafx.collections.ListChangeListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

/**
 * Takes the proposals from the append only store and makes Ballots out of it (vote is null).
 * Applies voting on individual ballots and persist the list.
 * The BallotList contains all ballots of all cycles.
 */
@Slf4j
public class BallotListService implements PersistedDataHost, DaoSetupService {
    public interface BallotListChangeListener {
        void onListChanged(List<Ballot> list);
    }

    private final ProposalService proposalService;
    private final PeriodService periodService;
    private final ProposalValidator proposalValidator;
    private final Storage<BallotList> storage;

    private final BallotList ballotList = new BallotList();
    private final List<BallotListChangeListener> listeners = new CopyOnWriteArrayList<>();

    @Inject
    public BallotListService(ProposalService proposalService, PeriodService periodService,
                             ProposalValidator proposalValidator, Storage<BallotList> storage) {
        this.proposalService = proposalService;
        this.periodService = periodService;
        this.proposalValidator = proposalValidator;
        this.storage = storage;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // DaoSetupService
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addListeners() {
        proposalService.getProposalPayloads().addListener((ListChangeListener<ProposalPayload>) c -> {
            c.next();
            if (c.wasAdded()) {
                c.getAddedSubList().stream()
                        .map(ProposalPayload::getProposal)
                        .filter(proposal -> ballotList.stream()
                                .noneMatch(ballot -> ballot.getProposal().equals(proposal)))
                        .forEach(proposal -> {
                            Ballot ballot = new Ballot(proposal); // vote is null
                            log.info("We create a new ballot with a proposal and add it to our list. " +
                                    "Vote is null at that moment. proposalTxId={}", proposal.getTxId());
                            if (!ballotList.contains(ballot)) {
                                ballotList.add(ballot);
                                listeners.forEach(l -> l.onListChanged(ballotList.getList()));
                            } else {
                                log.warn("Ballot already exist on our ballotList");
                            }
                        });
                persist();
            }
        });
    }

    @Override
    public void start() {
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PersistedDataHost
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void readPersisted() {
        if (BisqEnvironment.isDAOActivatedAndBaseCurrencySupportingBsq()) {
            BallotList persisted = storage.initAndGetPersisted(ballotList, 100);
            if (persisted != null) {
                ballotList.clear();
                ballotList.addAll(persisted.getList());
                listeners.forEach(l -> l.onListChanged(ballotList.getList()));
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void setVote(Ballot ballot, @Nullable Vote vote) {
        ballot.setVote(vote);
        persist();
    }

    public void addListener(BallotListChangeListener listener) {
        listeners.add(listener);
    }

    public List<Ballot> getValidatedBallotList() {
        return ballotList.stream()
                .filter(ballot -> proposalValidator.isTxTypeValid(ballot.getProposal()))
                .collect(Collectors.toList());
    }

    public List<Ballot> getValidBallotsOfCycle() {
        return ballotList.stream()
                .filter(ballot -> proposalValidator.isTxTypeValid(ballot.getProposal()))
                .filter(ballot -> periodService.isTxInCorrectCycle(ballot.getTxId(), periodService.getChainHeight()))
                .collect(Collectors.toList());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void persist() {
        storage.queueUpForSave();
    }
}
