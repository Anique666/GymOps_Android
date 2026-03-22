package com.example.gymmanagement.ui.member.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.repository.MemberRepository
import com.example.gymmanagement.di.AppContainer

class MemberListViewModel(application: Application) : AndroidViewModel(application) {

    private val memberRepository: MemberRepository = AppContainer(application).memberRepository

    val allMembers: LiveData<List<Member>> = memberRepository.getAllMembers()

    private var searchSource: LiveData<List<Member>>? = null
    private var isSearchMode = false

    private val _members = MediatorLiveData<List<Member>>()
    val members: LiveData<List<Member>> = _members

    init {
        _members.addSource(allMembers) { _members.value = it }
    }

    fun setSearchQuery(query: String) {
        searchSource?.let { _members.removeSource(it) }

        if (query.isBlank()) {
            if (isSearchMode) {
                _members.addSource(allMembers) { _members.value = it }
                isSearchMode = false
            }
            return
        }

        if (!isSearchMode) {
            _members.removeSource(allMembers)
            isSearchMode = true
        }

        val source = memberRepository.searchMembers(query.trim())
        searchSource = source
        _members.addSource(source) { _members.value = it }
    }

    fun deleteMember(member: Member) {
        memberRepository.deleteMember(member)
    }
}
