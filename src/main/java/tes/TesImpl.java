package tes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;
import interfaces.tes.Tes;

public class TesImpl<T extends State> implements Tes<T> {

	protected String name;
	protected List<ImmutableAttractor<T>> attractorsList;

	public TesImpl(List<ImmutableAttractor<T>> attractorsList) {
		this.attractorsList = attractorsList;
	}

	@Override
	public List<ImmutableAttractor<T>> getTesAttractors() {
		return this.attractorsList;
	}

	@Override
	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TesImpl<?> tes = (TesImpl<?>) o;
		return Objects.equals(name, tes.name) &&
				Objects.equals(attractorsList, tes.attractorsList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, attractorsList);
	}

	@Override
	public String toString() {
		//return "Tes [" + (name != null ? ("name=" + name + ", " ) : "") + "attIndices=" + attractorsList.stream().map(x -> x.getId()).collect(Collectors.toList()) + "]";
		return "Tes " + (name != null ? ( "\"" + name + "\"" + ", " ) : "") + "att=" + attractorsList.stream().map(x -> x.getId()).collect(Collectors.toList()) ;
	}




}
